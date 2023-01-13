package uk.gov.companieshouse.account.validator.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.account.validator.ApplicationConfiguration;
import uk.gov.companieshouse.account.validator.model.FileDetails;
import uk.gov.companieshouse.account.validator.service.AccountValidatedService;
import uk.gov.companieshouse.account.validator.service.AccountValidator;
import uk.gov.companieshouse.account.validator.service.AmazonFileTransfer;
import uk.gov.companieshouse.account.validator.service.TnepValidationService;
import uk.gov.companieshouse.account.validator.utility.filetransfer.FileTransferTool;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    private final FileTransferTool fileTransferTool;
    private final TnepValidationService tnepValidationService;

    private final IxbrlValidationImpl ixbrlValidationImpl;

    @Autowired
    public AccountValidatorImpl(FileTransferTool fileTransferTool,
                                TnepValidationService tnepValidationService,
                                IxbrlValidationImpl ixbrlValidationImpl,
                                ApplicationConfiguration config) {
        this.fileTransferTool = fileTransferTool;
        this.tnepValidationService = tnepValidationService;
        this.ixbrlValidationImpl = ixbrlValidationImpl;
        this._config = config;
    }

    /**
     * Downloads the ixbrl content and call the tnep validation service if the
     * download was successful. The tnep validation service needs the location
     * and the data to performs the validation.
     *
     * @param fileLocation - location of the file that needs to be validated.
     * @return true is valid ixbrl.
     */
    private boolean isValidIxbrl(String fileLocation) {

        boolean isIxbrlValid = false;
        String ixbrlData = downloadIxbrlFromLocation(fileLocation);


        if (ixbrlData != null) {
            isIxbrlValid = tnepValidationService.validate(ixbrlData, fileLocation);
        }

        return isIxbrlValid;
    }

    /**
     * Calls the fileTransferTool to download file from public location.
     *
     * @param location - the ixbrl location, which is a public location.
     * @return the actual ixbrl content. Or null if download fails.
     */
    private String downloadIxbrlFromLocation(String location) {

        String ixbrlData = fileTransferTool.downloadFileFromLocation(location);

        if (StringUtils.isEmpty(ixbrlData)) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_MESSAGE_KEY, "The ixbrl data content is empty");

            LOGGER.error("FilingServiceImpl: File Transfer Tool has fail to download file", logMap);

            return null;
        }

        return ixbrlData;
    }

    /**
     * Verifies should call TNDP (ZIP) file or iXBRL Validation
     *
     * @param fileDetails - the ixbrl location, which is a public location.
     * @return the actual ixbrl content. Or null if download fails.
     */
    public boolean downloadIxbrlFromLocation(FileDetails fileDetails) throws IOException {

        //S3 file location
        String location = generateS3FileLocation(fileDetails);

        //Option 1: Fetch ixbrl from AmazonFileTransfer from S3
        String ixbrlData = amazonFileTransfer.getIxbrlFromS3(location);

        //Option 2: Fetch file from FileTransferTool  from S3
//        String ixbrlData = fileTransferTool.downloadFileFromLocation(fileDetails.getFile_name());

        if (StringUtils.isEmpty(ixbrlData)) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_MESSAGE_KEY, "The ixbrl data content is empty");

            LOGGER.error("Account validator: Amazon File Transfer has fail to download file", logMap);
//            LOGGER.error("Account validator: File Transfer Tool has fail to download file", logMap);

            return false;
        }

        //Preparing the data for zip file check
        StreamSource inputDocStreamSource = new StreamSource(new ByteArrayInputStream(ixbrlData.getBytes()), "submission:/" + fileDetails.getFile_name());

        boolean isZipFile = isZipFile(inputDocStreamSource);

        //Validate against TNEP
        if (isZipFile) {
            return tnepValidationService.validate(ixbrlData, location);
        } else {
            return ixbrlValidationImpl.validate(ixbrlData, location);
        }
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

    private boolean isZipFile(StreamSource source) throws IOException {
        source.getInputStream().mark(Integer.MAX_VALUE);
        ZipInputStream zipInputStream = new ZipInputStream(source.getInputStream());
        boolean isZipFile = zipInputStream.getNextEntry() != null;
        source.getInputStream().reset();

        return isZipFile;
    }

}
