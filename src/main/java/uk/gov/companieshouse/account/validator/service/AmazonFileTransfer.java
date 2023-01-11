package uk.gov.companieshouse.account.validator.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.InputStream;
import java.net.URL;

public interface AmazonFileTransfer {

    AmazonS3 getAWSCredentials();

    void uploadFileInS3(AmazonS3 s3client, String key, InputStream inputStream, ObjectMetadata omd);

    URL generatePresignedUrl(AmazonS3 s3client,String objectKey);

    void deleteObjectInS3(AmazonS3 s3client, String key);
    
    String getIxbrlFromS3(String s3Location);
}
