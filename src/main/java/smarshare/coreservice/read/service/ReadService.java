package smarshare.coreservice.read.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import smarshare.coreservice.read.model.Bucket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReadService {

    private S3ReadService s3ReadService;
    private ObjectMapper jsonConverter;
    private List<Bucket> bucketList = null;

    @Autowired
    ReadService(S3ReadService s3ReadService, ObjectMapper jsonConverter) {
        this.s3ReadService = s3ReadService;
        this.jsonConverter = jsonConverter;
    }

    public List<Bucket> getBucketListFromS3() {
        log.info( "Inside getBucketListFromS3" );
        if (bucketList == null){
            bucketList = s3ReadService.listBuckets();
        }
        return bucketList;
    }

    public List<Bucket> getFilesAndFoldersListByUserAndBucket(String userName, String bucketName) {
        log.info( "Inside getFilesAndFoldersByUserAndBucket" );
        s3ReadService.listObjects( userName, bucketName );
        return null;
    }

    public Map<String, Resource> downloadFile(String objectName, String fileName, String bucketName) {
        log.info( "Inside downloadFile" );
        /* have to implement cache logic */
        // no need to lock the folder but have to check the lock status
        return s3ReadService.getObject( objectName, fileName, bucketName );
    }

    public List<Map<String, Resource>> downloadFolder(Map<String, Map<String, String>> fileNameWrapper) {
        log.info( "Inside downloadFolder" );
        // no need to lock the folder but have to check the lock status
        List<Map<String, Resource>> downloadedFiles = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> eachFile : fileNameWrapper.entrySet()) {
            eachFile.getValue().forEach( (objectName, bucketName) -> {
                downloadedFiles.add( s3ReadService.getObject( objectName, eachFile.getKey(), bucketName ) );
            } );
        }
        return (downloadedFiles);
    }

    //    @KafkaListener(groupId="readConsumer",topics = "read")
    public void consume(String bucketToBeUpdatedOrDeletedInInternalCache, ConsumerRecord record) throws IOException {
        System.out.println( "bucketToBeUpdatedInInternalCache------------->" + bucketToBeUpdatedOrDeletedInInternalCache );
        System.out.println( "record--------->" + record );

        if (record.key() == ("add")) {
            log.info( "Consumed Cache add Event" );
            Bucket bucketToBeAddedInCache = jsonConverter.readValue( bucketToBeUpdatedOrDeletedInInternalCache, Bucket.class );
            System.out.println( "result----file----->" + bucketToBeAddedInCache );
            if (!bucketList.isEmpty()) {
                bucketList.add( bucketToBeAddedInCache );
                log.info( "Bucket has been added in the cache" );
            }

        }
        if (record.key() == ("delete")) {
            log.info( "Consumed Cache delete Event" );
            Bucket bucketToBeDeletedInCache = jsonConverter.readValue( bucketToBeUpdatedOrDeletedInInternalCache, Bucket.class );
            System.out.println( "result----file----->" + bucketToBeDeletedInCache );
            if (!bucketList.isEmpty()) {
                bucketList.remove( bucketToBeDeletedInCache );
                log.info( "Bucket has been deleted from the cache" );
            }

        }
    }
}
