package uk.gov.companieshouse.account.validator.service.impl;

import org.apache.commons.fileupload.util.LimitedInputStream;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.account.validator.ApplicationConfiguration;
import uk.gov.companieshouse.account.validator.model.AccountValidated;
import uk.gov.companieshouse.account.validator.model.FileDetails;
import uk.gov.companieshouse.account.validator.model.ValidationResponse;
import uk.gov.companieshouse.account.validator.service.AccountValidatedService;
import uk.gov.companieshouse.account.validator.service.AccountValidator;
import uk.gov.companieshouse.account.validator.service.AmazonFileTransfer;
import uk.gov.companieshouse.account.validator.service.FelixValidationService;
import uk.gov.companieshouse.account.validator.validation.ixbrl.Results;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipInputStream;

@Service
public class AccountValidatorImpl implements AccountValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger("account-validator-api");
    private static final String LOG_MESSAGE_KEY = "message";
    private static final String LOG_ERROR_KEY = "error";
    private static final String LOCATION = "s3://%s/%s/%s";
    private static final String DOCUMENT_BUCKET_NAME = "DOCUMENT_BUCKET_NAME";
    private static final String DOCUMENT_RENDER_SERVICE_HOST = "DOCUMENT_RENDER_SERVICE_HOST";
    private String documentBucketName;

    @Autowired
    AccountValidatedService accountValidatedService;

    @Autowired
    private AmazonFileTransfer amazonFileTransfer;

    private final ApplicationConfiguration _config;
    private final FelixValidationService felixValidationService;

    @Autowired
    public AccountValidatorImpl(FelixValidationService felixValidationService,
                                ApplicationConfiguration config) {
        this.felixValidationService = felixValidationService;
        this._config = config;
    }

    /**
     * Downloads the ixbrl content and call the felix validation service if the
     * download was successful. The felix validation service needs the location
     * and the data to performs the validation.
     *
     * @param fileDetails - fileDetails details that needs to be validated.
     * @return true is valid ixbrl.
     */
    @Override
    public Results downloadIxbrlFromLocation(FileDetails fileDetails) throws IOException {

        //S3 file location
        String location = generateS3FileLocation(fileDetails);

        //Option 1: Fetch ixbrl from AmazonFileTransfer from S3
        String ixbrlData = amazonFileTransfer.getIxbrlFromS3(location);

        if (StringUtils.isEmpty(ixbrlData)) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_MESSAGE_KEY, "The ixbrl data content is empty");

            LOGGER.error("Account validator: Amazon File Transfer has fail to download file", logMap);

            return null;
        }

        //Preparing the data for zip file check
        StreamSource inputDocStreamSource = new StreamSource(new ByteArrayInputStream(ixbrlData.getBytes()), "submission:/" + fileDetails.getFile_name());

        boolean isZipFile = isZipFile(inputDocStreamSource);

        File file = createFile(ixbrlData, location);

        //Validate against Felix
        Results validated = felixValidationService.validate(ixbrlData, location);

        createAccountValidated(fileDetails, validated, !isZipFile);

//      TODO: Call ixbrl_rendere
        if (!isZipFile) {
            System.out.println("Should Call " + DOCUMENT_RENDER_SERVICE_HOST);
        }

        return validated;
    }

    private File createFile(String ixbrlData, String location) throws IOException {
        Path pathXMLFile = Paths.get(location);
        File file = Files.write(pathXMLFile, ixbrlData.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE).toFile();
        return file;
    }


    /**
     * Is pass the file from the request and call the felix validation service
     * The felix validation service needs the location
     * and the data to performs the validation.
     *
     * @param iXbrlData - file data from de request to be validated.
     * @param fileName  - file data from de request to be validated.
     * @return true is valid ixbrl.
     */
    @Override
    public Results validateFileDirect(String iXbrlData, String fileName) throws IOException {

        boolean isBase64Encoded = false;

        byte[] bytes = iXbrlData.getBytes(StandardCharsets.UTF_8);

        if (StringUtils.isEmpty(iXbrlData)) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_MESSAGE_KEY, "The ixbrl data content is empty");

            LOGGER.error("Account validator: Direct Validation has fail", logMap);
            throw new IOException("Account validator: he ixbrl data content is empty");
        }

        //Preparing the data for zip file check
        StreamSource inputDocStreamSource = new StreamSource(getImageContentAsStream(iXbrlData), "submission:/" + fileName);

        if (_config.getPlatformMaxDecodedSizeMB() > 0) {
            inputDocStreamSource = wrapInBuffer(wrapInSizeLimiter(inputDocStreamSource, isBase64Encoded));
        } else {
            inputDocStreamSource = wrapInBuffer(inputDocStreamSource);
        }

        boolean isZipFile = isZipFile(inputDocStreamSource);

        //Validate against Felix
        Results validated = felixValidationService.validate(iXbrlData, "submission:/");

//      TODO: Call ixbrl_renderer
        if (!isZipFile) {
            System.out.println("Should Call " + DOCUMENT_RENDER_SERVICE_HOST);
        }

        return validated;
    }

    private StreamSource wrapInBuffer(StreamSource source) {
        return new StreamSource(new BufferedInputStream(source.getInputStream()), source.getSystemId());
    }

    public StreamSource wrapInSizeLimiter(final StreamSource source, final boolean base64) {
        return new StreamSource(new LimitedInputStream(source.getInputStream(), (long) _config.getPlatformMaxDecodedSizeMB() * 1024 * 1024) {
            @Override
            protected void raiseError(long pSizeMax, long pCount) {
                throw new RuntimeException(String.format("Uploaded document is too large%1$s.  Maximum permitted size%1$s is %2$sMB.", base64 ? " after base64 decoding" : "", _config.getPlatformMaxDecodedSizeMB()));
                // This comes out an XML response with HTTP code 200.  That appears unRESTful but we are considering the size limit to be a business rule not a size limit at the HTTP level.
            }
        }, source.getSystemId());
    }

    /**
     * Generate the document containing the accounts details and return the location of the document
     *
     * @param fileDetails FileDetails S3_key, customer_id, File name
     * @return location or null if document generation failed
     * @throws IOException
     */
    private String generateS3FileLocation(FileDetails fileDetails) {

        String bucketName = getDocumentBucketName();

        if (StringUtils.isNotBlank(bucketName)) {

            return String.format(LOCATION, bucketName, fileDetails.getCustomer_id(), fileDetails.getS3_key() + fileDetails.getFile_name());

        } else {

            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_ERROR_KEY, "S3 file location generation failure");
            logMap.put(LOG_MESSAGE_KEY, "The bucket name to store the generated document has not been configured");

            LOGGER.error("S3 file location generation failure", logMap);
        }

        return null;
    }

    /**
     * Get the name of the bucket to store the generated document
     *
     * @return documentBucketName
     */
    private String getDocumentBucketName() {

        if (documentBucketName == null) {

            documentBucketName = System.getenv(DOCUMENT_BUCKET_NAME);
        }

        return documentBucketName;
    }

    /**
     * Verifies (ZIP) file or Plain HMTL
     *
     * @param source - the file.
     * @return then returns false ou true.
     */
    private boolean isZipFile(StreamSource source) throws IOException {
        source.getInputStream().mark(Integer.MAX_VALUE);
        ZipInputStream zipInputStream = new ZipInputStream(source.getInputStream());
        boolean isZipFile = zipInputStream.getNextEntry() != null;
        source.getInputStream().reset();

        return isZipFile;
    }

    private void createAccountValidated(FileDetails fileDetails, Results result, boolean showImage) {
        String uuid = UUID.randomUUID().toString();
        AccountValidated accountValidated = new AccountValidated();
        accountValidated.setId(uuid);
        accountValidated.setCustomerId(fileDetails.getCustomer_id());
        accountValidated.setFilename(fileDetails.getFile_name());
        accountValidated.setS3Key(fileDetails.getS3_key());

        ValidationResponse validationResponse = new ValidationResponse();
        validationResponse.setId(uuid);
        validationResponse.setValid(result.getValidationStatus().equalsIgnoreCase("success"));
        validationResponse.setTndpResponse(result.getValidationStatus().equalsIgnoreCase("success") ? "OK" : "Failed");

        accountValidated.setData(validationResponse);
        accountValidated.setShowImage(showImage);

        accountValidatedService.createAccount(accountValidated);
    }

    @Override
    public InputStream getImageContentAsStream(Object data) {
        if (data instanceof String) {
            String xrblData = data.toString();
            return new ByteArrayInputStream(xrblData.getBytes());
        } else {
            return (InputStream) data;
        }
    }

    public static File convertToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

}
