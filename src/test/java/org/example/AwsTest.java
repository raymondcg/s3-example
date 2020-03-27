package org.example;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.AppConfig;
import org.example.config.ApplicationProperties;
import org.example.dao.DataDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { AppConfig.class, ApplicationProperties.class })
public class AwsTest {

    private static final Logger LOGGER = LogManager.getLogger( AwsTest.class );

    @Autowired
    private DataDao dataDao;

    @Test
    public void saveFile() throws IOException {
        String fileName = "tempFile.txt";
        dataDao.deleteObject( ApplicationProperties.BUCKET_NAME, fileName );
        File file = new File( fileName );

        // Write something into file.
        FileUtils.writeStringToFile( file, "hello dudes and world", StandardCharsets.UTF_8 );

        dataDao.createObject( ApplicationProperties.BUCKET_NAME, file );
        File returned = dataDao.downloadObject( ApplicationProperties.BUCKET_NAME, fileName );
        assertTrue( file.equals( returned ) );
    }

    @Test
    public void deleteFile() throws IOException {
        // Delete the file if it already exists.
        String fileName = "fileToDelete.txt";
        File file = new File( fileName );

        // Write something into file.
        FileUtils.writeStringToFile( file, "hello dudes and world", StandardCharsets.UTF_8 );

        dataDao.createObject( ApplicationProperties.BUCKET_NAME, file );
        dataDao.deleteObject( ApplicationProperties.BUCKET_NAME, fileName );
        dataDao.doesObjectExist( ApplicationProperties.BUCKET_NAME, fileName );
        assertFalse( dataDao.doesObjectExist( ApplicationProperties.BUCKET_NAME, fileName ) );
    }

    @Test
    public void saveStream() throws IOException {
        String fileName = "tempFile.txt";
        dataDao.deleteObject( ApplicationProperties.BUCKET_NAME, fileName );
        File file = new File( fileName );

        // Write something into file.
        FileUtils.writeStringToFile( file, "hello dudes and world", StandardCharsets.UTF_8 );

        try (InputStream inputStream = new FileInputStream( file )) {
            dataDao.createObject( ApplicationProperties.BUCKET_NAME, fileName, inputStream );
        }
        File returned = dataDao.downloadObject( ApplicationProperties.BUCKET_NAME, fileName );
        assertTrue( file.equals( returned ) );
    }
}