package uk.gov.companieshouse.account.validator.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.account.validator.service.AccountValidator;
import uk.gov.companieshouse.account.validator.service.TnepValidationService;
import uk.gov.companieshouse.account.validator.utility.filetransfer.FileTransferTool;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AccountValidatorImpl implements AccountValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger("account-validator-api");
    private static final String LOG_MESSAGE_KEY = "message";
    private final FileTransferTool fileTransferTool;
    private final TnepValidationService tnepValidationService;

    @Autowired
    public AccountValidatorImpl(FileTransferTool fileTransferTool,
                                TnepValidationService tnepValidationService){
        this.fileTransferTool = fileTransferTool;
        this.tnepValidationService = tnepValidationService;
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
     * @param location
     *            - the ixbrl location, which is a public location.
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
}
