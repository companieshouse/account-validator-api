package uk.gov.companieshouse.account.validator.service.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.Protocol;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.account.validator.model.AWSServiceProperties;
import uk.gov.companieshouse.account.validator.service.AmazonFileTransfer;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.account.validator.utility.Constants.AWS_REGION_ENV_VAR;

@Component
public class AmazonFileTransferImpl implements AmazonFileTransfer {

    private static final String SUFFIX = "/";
    private static final String S3_PATH_PREFIX = "s3://";
    private static final String FOLDER_ERROR_MESSAGE = "Subfolder path does not exist";
    private static final String PROXY_HOST_PROPERTY = "IMAGE_CLOUD_PROXY_HOST";
    private static final String PROXY_PORT_PROPERTY = "IMAGE_CLOUD_PROXY_PORT";

    private static final int SPLIT_S3_PATH_SUBSTRING=5;
    private static final int GET_S3_PATH_FROM_SPLIT=2;
    
    private static final Logger LOG = LoggerFactory.getLogger("abridged.accounts.api.ch.gov.uk");

    private EnvironmentReader environmentReader;
    private AWSServiceProperties configuration;

    @Autowired
    public AmazonFileTransferImpl(AWSServiceProperties configuration, EnvironmentReader environmentReader) {
        this.configuration = configuration;
        this.environmentReader = environmentReader;
    }

    /**
     * Get the AWS credentials
     */
    @Override
    public AmazonS3 getAWSCredentials() {
        AWSCredentials credentials = new BasicAWSCredentials(configuration.getAccessKeyId(),
                configuration.getSecretAccessKey());
        return getAmazonS3(credentials);
    }

    /**
     * Get the bucket name on S3
     */
    private String getAWSBucketName() {
        return getSplitS3path()[0];
    }

    private String getPathIfExists() {
        if (getSplitS3path().length > 1) {
            return getSplitS3path()[1] + SUFFIX;
        } else {
            return "";
        }
    }

    /**
     * Get the S3 path split
     */
    private String[] getSplitS3path() {
        String s3Path = configuration.getS3Path();
        String path = s3Path.substring(SPLIT_S3_PATH_SUBSTRING);
        return path.split("/", GET_S3_PATH_FROM_SPLIT);
    }

    /**
     * Upload the file in S3
     */
    @Override
    public void uploadFileInS3(AmazonS3 s3client, String fileName, InputStream inputStream, ObjectMetadata omd) {
        validateS3Details(s3client);
        if (validatePathExists(s3client)) {
            s3client.putObject(new PutObjectRequest(getAWSBucketName(), getKey(fileName), inputStream, omd));
        } else {
            throw new SdkClientException(FOLDER_ERROR_MESSAGE);
        }
    }

    /**
     * Configure the S3 client
     * 
     * @param credentials
     * @return An {@link AmazonS3}
     */
    private AmazonS3 getAmazonS3(AWSCredentials credentials) {
        ClientConfiguration clientConfiguration = getClientConfiguration(getProxyHost(),
                                                                         getProxyPort(),
                                                                         configuration.getProtocol());
        return new AmazonS3Client(credentials, clientConfiguration);
    }

    /**
     * Get the proxy host if it has been defined
     *
     * @return A {@link String} or null
     */
    private String getProxyHost() {
        return System.getenv(PROXY_HOST_PROPERTY);
    }

    /**
     * Get the proxy port if it has been defined
     *
     * @return An {@link Integer} or null
     */
    private Integer getProxyPort() {
        String proxyPortString = System.getenv(PROXY_PORT_PROPERTY);
        return proxyPortString == null ? null : Integer.valueOf(proxyPortString);
    }

    /**
     * Get a Temporary URL to get the data on S3 . The URL has an expiration
     * time of an hour.
     * 
     */
    @Override
    public URL generatePresignedUrl(AmazonS3 s3client, String fileName) {
        validateS3Details(s3client);
        if (validatePathExists(s3client)) {
            Regions regions = Regions.fromName(environmentReader.getMandatoryString(AWS_REGION_ENV_VAR));
            Region region = Region.getRegion(regions);
            s3client.setRegion(region);

            // Add an expiration of an hour
            Date expiration = new Date();
            long msec = expiration.getTime();
            msec += 1000 * 60 * 60;
            expiration.setTime(msec);

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                    getAWSBucketName(), getKey(fileName));
            generatePresignedUrlRequest.setMethod(HttpMethod.GET);
            generatePresignedUrlRequest.setExpiration(expiration);

            return s3client.generatePresignedUrl(generatePresignedUrlRequest);
        } else {
            throw new SdkClientException(FOLDER_ERROR_MESSAGE);
        }

    }

    /**
     * Delete an object in S3
     * 
     * @throws SdkClientException
     */
    @Override
    public void deleteObjectInS3(AmazonS3 s3client, String fileName) {
        validateS3Details(s3client);
        if (validatePathExists(s3client)) {
            s3client.deleteObject(new DeleteObjectRequest(getAWSBucketName(), getKey(fileName)));
        } else {
            throw new SdkClientException(FOLDER_ERROR_MESSAGE);
        }
    }

    /**
     * Get the AWS credentials
     */
    protected AmazonS3 getAmazonS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(configuration.getAccessKeyId(),
                configuration.getSecretAccessKey());
        return getAmazonS3Client(credentials);
    }

    /**
     * Configure the S3 client
     * 
     * @param credentials
     * @return An {@link AmazonS3}
     */
    private AmazonS3 getAmazonS3Client(AWSCredentials credentials) {
        ClientConfiguration clientConfiguration = getClientConfiguration(getProxyHost(),
                                                                         getProxyPort(),
                                                                         configuration.getProtocol());
        return new AmazonS3Client(credentials, clientConfiguration);
    }

    /**
     * Get the bucket name from the S3 location
     */
    protected String getBucketFromS3Location(String location) {
        String path = location.substring(SPLIT_S3_PATH_SUBSTRING);
        return path.split("/")[0];
    }

    /**
     * Get the key from the S3 location
     */
    protected String getKeyFromS3Location(String location) {
        String path = location.substring(SPLIT_S3_PATH_SUBSTRING);
        String[] pathSegments = path.split("/");
        
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 1; i < pathSegments.length-1 ; i++) {
            stringBuffer.append(pathSegments[i]);
            stringBuffer.append('/');
        }
        stringBuffer.append(pathSegments[pathSegments.length-1]);
        
        return stringBuffer.toString();
    }

    /**
     * Configure the Proxy Host Proxy port and the Protocol
     * 
     * @param httpProxyHostName
     * @param httpProxyPort
     * @param protocol
     * @return A {@link ClientConfiguration}
     */
    protected ClientConfiguration getClientConfiguration(String httpProxyHostName, Integer httpProxyPort, String protocol) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        if (httpProxyHostName != null && !httpProxyHostName.trim().isEmpty()) {
            clientConfiguration.setProxyHost(httpProxyHostName);
        }

        if (httpProxyPort != null) {
            clientConfiguration.setProxyPort(httpProxyPort);
        }

        clientConfiguration.setProtocol("http".equalsIgnoreCase(protocol) ? Protocol.HTTP : Protocol.HTTPS);

        return clientConfiguration;
    }
    
    /**
     * Get ixbrl from S3
     * 
     * @return String
     */
    @Override
    public String getIxbrlFromS3(String s3Location) {    
        try {
            AmazonS3 s3client = getAmazonS3Client();
            S3Object s3Object = getObjectInS3(s3Location, s3client);
            byte[] byteArray = IOUtils.toByteArray(s3Object.getObjectContent());
            return new String(byteArray);
        } catch (Exception e) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("error", "Unable to fetch ixbrl from S3");
            LOG.error(e, logMap);
            return null;
        }
    }
    
    /**
     * Get an object in S3
     * 
     * @throws SdkClientException
     */
    protected S3Object getObjectInS3(String location, AmazonS3 s3client) {
        
        String bucket = getBucketFromS3Location(location);
        String key = getKeyFromS3Location(location);
        
        if (!s3client.doesObjectExist(bucket, key))
            throw new SdkClientException("S3 path does not exist: " + location);

        return s3client.getObject(new GetObjectRequest(bucket, key));
    }
    
    private String getKey(String fileName) {
        String key;
        if (getSplitS3path().length > 1) {
            key = getPathIfExists() + fileName;
        } else {
            key = fileName;
        }
        return key;
    }

    private boolean validateS3Path() {
        return configuration.getS3Path().trim().toLowerCase().startsWith(S3_PATH_PREFIX);
    }

    private boolean validateBucketName() {
        return !getAWSBucketName().isEmpty();
    }

    private void validateS3Details(AmazonS3 s3client) {
        if (!validateS3Path())
            throw new SdkClientException("S3 path is invalid");
        if (!validateBucketName())
            throw new SdkClientException("bucket name is invalid");
        if (!s3client.doesBucketExist(getAWSBucketName()))
            throw new SdkClientException("bucket does not exist");
    }

    private boolean validatePathExists(AmazonS3 s3client) {
        return getPathIfExists().isEmpty() || s3client.doesObjectExist(getAWSBucketName(), getPathIfExists().trim());
    }
}
