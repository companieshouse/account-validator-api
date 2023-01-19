package uk.gov.companieshouse.account.validator.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.account.validator.service.FelixValidationService;
import uk.gov.companieshouse.account.validator.validation.ixbrl.Results;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static uk.gov.companieshouse.account.validator.AccountValidatorApplication.APPLICATION_NAME_SPACE;

@Service
public class FelixValidationServiceImpl implements FelixValidationService {

    private String boundary;
    private static final String LINE = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    private static final String FELIX_VALIDATOR_URI = "FELIX_VALIDATOR_URI";

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private EnvironmentReader environmentReader;

    private String felixUri;

    @Autowired
    public FelixValidationServiceImpl(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
        this.felixUri = getFelixValidatorUri();
    }

    /**
     * Validate the ixbrl
     *
     * @return boolean
     */
    @Override
    public Results validate(String iXbrlData, String location) {

        LOGGER.info("FelixValidationServiceImpl: Ixbrl validation has started");
        try {
            return validatIxbrlAgainstFelix(iXbrlData, location);
        } catch (Exception e) {
            LOGGER.error(String.format("Exception has been thrown when calling FELIX validator: %s ", e.getMessage()));
        }

        LOGGER.info("FelixValidationServiceImpl: Ixbrl validation has finished");

        return null;
    }

    /**
     * Call FELIX validator service, via http POST using multipart file upload, to check if ixbrl is
     * valid.
     *
     * @param ixbrl    - ixbrl content to be validated.
     * @param location - location of the file.
     * @return {@link Results} with the information from calling the Felix service.
     */
    private Results validatIxbrlAgainstFelix(String ixbrl, String location){
        try {
            // Set header
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
            return postForValidation(headers, ixbrl, location);
        } catch (Exception e) {
            LOGGER.error(String.format("ValidatIxbrlAgainstFelix: %s", e.getMessage()));
        }
        return null;

    }

    /**
     * Connect to the FELIX validator via http POST using multipart file upload
     *
     * @param headers    - request headers.
     * @param ixblrData    - ixbrl content to be validated.
     * @param location - location of the file.
     * @return {@link Results} with the information from calling the Felix service.
     * @throws IOException
     */
    private Results postForValidation(Map<String, String> headers, String ixblrData, String location)
            throws IOException {

        this.charset = "utf-8";
        boundary = UUID.randomUUID().toString();

        URL url = new URL(this.felixUri);

        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);    // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        if (headers != null && headers.size() > 0) {
            Iterator<String> it = headers.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = headers.get(key);
                httpConn.setRequestProperty(key, value);
            }
        }

        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);

        Path pathXMLFile = Paths.get(location);
        addFilePart("file", new File(pathXMLFile.toUri()), ixblrData);

        String response = null;
        try {
            response = completeTheRequest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        ObjectMapper objectMapper = new XmlMapper();
        Results Results = objectMapper.readValue(response, Results.class);

        return Results;
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName     - file name.
     * @param fileToUpload  - file to upload.
     * @param ixblrData     - ixbrl content to be validated.
     * @return {@link Results} with the information from calling the Felix service.
     * @throws IOException
     */
    public void addFilePart(String fieldName, File fileToUpload, String ixblrData)
            throws IOException {
        String fileName = fileToUpload.getName();
        writer.append("--" + boundary).append(LINE);
        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE);
        writer.append("Content-Transfer-Encoding: binary").append(LINE);
        writer.append(LINE);
        writer.flush();

        InputStream inputStream = new ByteArrayInputStream(ixblrData.getBytes(StandardCharsets.UTF_8));
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        writer.append(LINE);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return String as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public String completeTheRequest() throws IOException {
        String response = "";
        writer.flush();
        writer.append("--" + boundary + "--").append(LINE);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = httpConn.getInputStream().read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            response = result.toString(this.charset);
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        return response;
    }

    /**
     * Obtain the URL of the FELIX validator from the environment
     *
     * @return String
     */
    protected String getFelixValidatorUri() {

        return environmentReader.getMandatoryString(FELIX_VALIDATOR_URI);
    }
}
