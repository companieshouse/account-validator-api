package uk.gov.companieshouse.account.validator.service;

import org.apache.commons.lang.StringUtils;
import uk.gov.companieshouse.account.validator.model.FileDetails;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

public interface AccountValidator {
    boolean downloadIxbrlFromLocation(FileDetails fileDetails) throws IOException;

    boolean validateFileDirect(String iXbrlData, String fileName) throws IOException;

    InputStream getImageContentAsStream(Object data);
}


