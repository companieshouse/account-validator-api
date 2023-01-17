package uk.gov.companieshouse.account.validator.service.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.account.validator.model.AWSServiceProperties;
import uk.gov.companieshouse.environment.EnvironmentReader;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.account.validator.utility.Constants.AWS_REGION_ENV_VAR;

class AmazonFileTransferTest {
    private AmazonFileTransferImpl amazonFileTransfer;
    private AWSServiceProperties configuration;
    private ServletInputStream inputStream;
    private ObjectMetadata omd;
    private AmazonS3Client client;
    private AmazonS3 amazonS3;
    private S3Object s3Object;
    private S3ObjectInputStream s3ObjectInputStream;
    private EnvironmentReader environmentReader;
    private PutObjectRequest putObjectRequest;
    private PutObjectResult putObjectResult;
    private DeleteObjectRequest deleteObjectRequest;
    private static final String  S3_PATH = "s3://document-api-ixbrl/hamish";
    private static final String INVALID_PATH= "document-api-ixbrl";
    private static final String S3_PATH_WITHOUT_BUCKET_NAME="s3://";
    private static final String BUCKET_NAME="document-api-ixbrl";
    private static final String PATH_DIRECTORY="hamish/";
    private static final String  FILE_NAME = "fileName";
    private static final String MOCK_URL = "http://mock-path-to-ixbrl";
    private static final String S3_LOCATION = "s3://account-validator-bucket/accounts-validator/accounts19a5c3bd-4e54-49b1-915d-632f12036372.html";

    private static final String MOCK_AWS_REGION = "eu-west-1";
    
    @BeforeEach
    public void setUp() {
        configuration = mock(AWSServiceProperties.class);
        putObjectRequest = mock(PutObjectRequest.class);
        deleteObjectRequest = mock(DeleteObjectRequest.class);
        putObjectResult = mock(PutObjectResult.class);
        client = mock(AmazonS3Client.class);
        omd = mock(ObjectMetadata.class);
        inputStream = mock(ServletInputStream.class);
        
        amazonS3 = mock(AmazonS3.class);
        s3Object = mock(S3Object.class);
        configuration = mock(AWSServiceProperties.class);
        s3ObjectInputStream = mock(S3ObjectInputStream.class);
        environmentReader = mock(EnvironmentReader.class);

        amazonFileTransfer = new AmazonFileTransferImpl(configuration, environmentReader) {
            @Override
            protected AmazonS3 getAmazonS3Client() {
                return amazonS3;
            }
        };
    }

    private void mockConfigurationDetails(){
        when(configuration.getAccessKeyId()).thenReturn("");
        when(configuration.getSecretAccessKey()).thenReturn("");
        when(configuration.getProtocol()).thenReturn("");
        when(configuration.getS3Path()).thenReturn(S3_PATH);
        when(client.doesBucketExist(BUCKET_NAME)).thenReturn(true);
        when(client.doesObjectExist(BUCKET_NAME, PATH_DIRECTORY)).thenReturn(true);
    }

    private void mockEnvironmentReader() {
        when(environmentReader.getMandatoryString(AWS_REGION_ENV_VAR)).thenReturn(MOCK_AWS_REGION);
    }

    @Test
    void testGetAWSCredentials() {
        mockConfigurationDetails();
        AmazonS3 s3 = amazonFileTransfer.getAWSCredentials();
        assertNotNull(s3);
        verify(configuration).getAccessKeyId();
        verify(configuration).getSecretAccessKey();
    }


    @Test
    void testGeneratePresignedUrl() throws MalformedURLException {
        mockConfigurationDetails();
        mockEnvironmentReader();
        URL mockUrl = new URL(MOCK_URL);
        when(client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);
        URL url = amazonFileTransfer.generatePresignedUrl(client,FILE_NAME);
        assertNotNull(url);
    }

    @Test
    void testUploadFileInS3() {
        mockConfigurationDetails();
        when(client.putObject(putObjectRequest)).thenReturn(putObjectResult);
        amazonFileTransfer.uploadFileInS3(client, FILE_NAME, inputStream, omd);
        verify(client).putObject(any(PutObjectRequest.class));
    }
    @Test
    void testUploadFileInS3InvalidS3Path() {
        mockConfigurationDetails();
        when(configuration.getS3Path()).thenReturn(INVALID_PATH);
        when(client.putObject(putObjectRequest)).thenReturn(putObjectResult);
        assertThrows(SdkClientException.class, () ->  amazonFileTransfer.uploadFileInS3(client, FILE_NAME, inputStream, omd));
    }
    @Test
    void testUploadFileInS3EmptyBucketName() {
        mockConfigurationDetails();
        when(configuration.getS3Path()).thenReturn(S3_PATH_WITHOUT_BUCKET_NAME);
        when(client.putObject(putObjectRequest)).thenReturn(putObjectResult);
        assertThrows(SdkClientException.class, () ->  amazonFileTransfer.uploadFileInS3(client, FILE_NAME, inputStream, omd));
    }
    @Test
    void testUploadFileInS3BucketDoesNotExists() {
        mockConfigurationDetails();
        when(client.doesBucketExist(BUCKET_NAME)).thenReturn(false);
        when(client.putObject(putObjectRequest)).thenReturn(putObjectResult);
        assertThrows(SdkClientException.class, () ->  amazonFileTransfer.uploadFileInS3(client, FILE_NAME, inputStream, omd));
    }
    @Test
    void testUploadFileInS3SubFolderPathDoesNotExist() {
        mockConfigurationDetails();
        when(client.doesObjectExist(BUCKET_NAME,PATH_DIRECTORY)).thenReturn(false);
        when(client.putObject(putObjectRequest)).thenReturn(putObjectResult);
        assertThrows(SdkClientException.class, () ->  amazonFileTransfer.uploadFileInS3(client, FILE_NAME, inputStream, omd));
    }
    @Test
    void testDeleteFileInS3() {
        mockConfigurationDetails();
        doNothing().when(client).deleteObject(deleteObjectRequest);
        amazonFileTransfer.deleteObjectInS3(client, FILE_NAME);
        verify(client).deleteObject(any(DeleteObjectRequest.class));
    }
    
    @Test
    void getIxbrlFromS3WhenLocationExists() throws IOException {

        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(true);
        when(amazonS3.getObject(any())).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);
        when(s3ObjectInputStream.read(any())).thenReturn(-1);
        
        String ixbrl = amazonFileTransfer.getIxbrlFromS3("s3://test.blah");
        
        assertEquals("", ixbrl);
    }
    
    @Test
    void getIxbrlFromS3WhenLocationNotFound() {

        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(false);
        
        String ixbrl = amazonFileTransfer.getIxbrlFromS3("s3://test.blah");
        
        assertEquals(null, ixbrl);
    }
    
    @Test
    void getObjectInS3WhenLocationNotFound() {

        when(amazonS3.doesObjectExist("test", "blah")).thenReturn(false);

        assertThrows(SdkClientException.class, () ->  amazonFileTransfer.getObjectInS3("s3://test/blah", amazonS3));

    }
    
    @Test
    void getObjectInS3WhenLocationExists() {

        when(amazonS3.doesObjectExist("test", "blah")).thenReturn(true);
        when(amazonS3.getObject(any())).thenReturn(s3Object);
        
        S3Object testS3Object = amazonFileTransfer.getObjectInS3("s3://test/blah", amazonS3);
        
        verify(amazonS3, times(1)).doesObjectExist("test", "blah");
        
        assertEquals(s3Object, testS3Object);
    }
    
    @Test
    void getBucketFromS3Location() {
        String bucket = amazonFileTransfer.getBucketFromS3Location(S3_LOCATION);
        assertEquals("account-validator-bucket", bucket);
    }
    
    @Test
    void getKeyFromS3Location() {
        String key = amazonFileTransfer.getKeyFromS3Location(S3_LOCATION);
        assertEquals("accounts-validator/accounts19a5c3bd-4e54-49b1-915d-632f12036372.html", key);
    }
    
    @Test
    void getClientConfigurationWithProxy() {
        
        ClientConfiguration clientConfiguration = amazonFileTransfer.getClientConfiguration("proxyhost", 123, "http");
        assertEquals(Protocol.HTTP, clientConfiguration.getProtocol());
        assertEquals("proxyhost", clientConfiguration.getProxyHost());
        assertEquals(123, clientConfiguration.getProxyPort());
    }
    
    @Test
    void getClientConfigurationNoProxy() {
        
        ClientConfiguration clientConfiguration = amazonFileTransfer.getClientConfiguration(null, null, null);
        assertEquals(Protocol.HTTPS, clientConfiguration.getProtocol());
        assertEquals(null, clientConfiguration.getProxyHost());
        assertEquals(-1, clientConfiguration.getProxyPort());
    }
    
    @Test
    void getAmazonS3Client() {
        AmazonFileTransferImpl testAmazonFileTransfer = new AmazonFileTransferImpl(configuration, environmentReader);
        
        when(configuration.getAccessKeyId()).thenReturn("");
        when(configuration.getSecretAccessKey()).thenReturn("");
        
        AmazonS3 testS3Client = testAmazonFileTransfer.getAmazonS3Client();
        
        verify(configuration, times(1)).getAccessKeyId();
        verify(configuration, times(1)).getSecretAccessKey();
        verify(configuration, times(1)).getProtocol();

        assertNotNull(testS3Client);
    }
}
