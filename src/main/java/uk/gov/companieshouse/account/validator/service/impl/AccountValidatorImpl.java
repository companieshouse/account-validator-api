package uk.gov.companieshouse.account.validator.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.account.validator.ApplicationConfiguration;
import uk.gov.companieshouse.account.validator.model.FileDetails;
import uk.gov.companieshouse.account.validator.service.AccountValidatedService;
import uk.gov.companieshouse.account.validator.service.AccountValidator;
import uk.gov.companieshouse.account.validator.service.AmazonFileTransfer;
import uk.gov.companieshouse.account.validator.service.FelixValidationService;
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
    private final FelixValidationService felixValidationService;

    private final IxbrlValidationImpl ixbrlValidationImpl;

    @Autowired
    public AccountValidatorImpl(FelixValidationService felixValidationService,
                                IxbrlValidationImpl ixbrlValidationImpl,
                                ApplicationConfiguration config) {
        this.felixValidationService = felixValidationService;
        this.ixbrlValidationImpl = ixbrlValidationImpl;
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
    public boolean downloadIxbrlFromLocation(FileDetails fileDetails) throws IOException {

        //S3 file location
        String location = generateS3FileLocation(fileDetails);

        //Option 1: Fetch ixbrl from AmazonFileTransfer from S3
        String ixbrlData = amazonFileTransfer.getIxbrlFromS3(location);

        if (StringUtils.isEmpty(ixbrlData)) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_MESSAGE_KEY, "The ixbrl data content is empty");

            LOGGER.error("Account validator: Amazon File Transfer has fail to download file", logMap);

            return false;
        }

        //Preparing the data for zip file check
        StreamSource inputDocStreamSource = new StreamSource(new ByteArrayInputStream(ixbrlData.getBytes()), "submission:/" + fileDetails.getFile_name());

        boolean isZipFile = isZipFile(inputDocStreamSource);

        //Validate against TNEP
        if (isZipFile) {
            return felixValidationService.validate(ixbrlData, location);
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

}
