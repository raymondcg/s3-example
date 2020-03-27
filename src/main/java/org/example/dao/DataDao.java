package org.example.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.Md5Utils;
import com.amazonaws.util.StringUtils;

public class DataDao {

    private static final Logger LOGGER = LogManager.getLogger( DataDao.class );

    private AmazonS3 s3Client;

    public DataDao( String endpoint, String accessKey, String secretKey ) {
        AWSCredentials credentials = new BasicAWSCredentials( accessKey, secretKey );
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials( new AWSStaticCredentialsProvider( credentials ) )
                .withClientConfiguration( getClientConfiguration() )
                .withEndpointConfiguration( getEndpointConfiguration( endpoint ) ).withPathStyleAccessEnabled( true )
                .build();
    }

    private EndpointConfiguration getEndpointConfiguration( String endpoint ) {
        // TODO must be a better way to do SigV4 signing region
        return new EndpointConfiguration( endpoint, "" );
    }

    private ClientConfiguration getClientConfiguration() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        // TODO should be https
        clientConfig.setProtocol( Protocol.HTTP );
        clientConfig.setRetryPolicy( new RetryPolicy( null, null, 5, false ) );
        return clientConfig;
    }

    public List<String> getBucketContents( String bucketName ) {
        List<String> results = new ArrayList<String>();
        ObjectListing objects = s3Client.listObjects( bucketName );
        do {
            for ( S3ObjectSummary objectSummary : objects.getObjectSummaries() ) {
                results.add( String.format( "%s\t%s\t%s", objectSummary.getKey(), objectSummary.getSize(),
                        StringUtils.fromDate( objectSummary.getLastModified() ) ) );
            }
            objects = s3Client.listNextBatchOfObjects( objects );
        } while ( objects.isTruncated() );
        return results;
    }

    public void createObject( String bucketName, File file ) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest( bucketName, file.getName(), file );
            s3Client.putObject( putObjectRequest );
        } catch ( SdkClientException ex ) {
            LOGGER.error( "Error saving file to S3.", ex );
        }
    }

    public File downloadObject( String bucketName, String fileName ) {
        File file = new File( fileName );
        try {
            if ( s3Client.doesObjectExist( bucketName, fileName ) ) {
                GetObjectRequest getObjectRequest = new GetObjectRequest( bucketName, fileName );
                s3Client.getObject( getObjectRequest, file );
            }
        } catch ( SdkClientException ex ) {
            LOGGER.error( "Error saving file to S3.", ex );
        }
        return file;
    }

    public void deleteObject( String bucketName, String fileName ) {
        try {
            s3Client.deleteObject( bucketName, fileName );
        } catch ( SdkClientException ex ) {
            LOGGER.error( "Error saving file to S3.", ex );
        }
    }

    public void createObject( String bucketName, String fileName, InputStream inputStream ) {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            // TODO MD5
            String md5 = DigestUtils.md5Hex( inputStream );
            objectMetadata.setContentMD5( Md5Utils.md5AsBase64( inputStream ) );

            // TODO file size

            PutObjectRequest putObjectRequest = new PutObjectRequest( bucketName, fileName, inputStream,
                    objectMetadata );
            s3Client.putObject( putObjectRequest );
        } catch ( SdkClientException ex ) {
            LOGGER.error( "Error saving file to S3.", ex );
        } catch ( IOException ex ) {
            LOGGER.error( "Error saving file to S3.", ex );
        }
    }

}
