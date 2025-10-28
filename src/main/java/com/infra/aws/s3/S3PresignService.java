package com.infra.aws.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3PresignService {
    private final Region region = Region.AP_NORTHEAST_2;

    public String presignedGetUrl(String bucket, String key, Duration ttl) {
        try (S3Presigner presigner = S3Presigner.builder().region(region).build()) {
            GetObjectRequest get = GetObjectRequest.builder()
                    .bucket(bucket).key(key).build();
            GetObjectPresignRequest preq = GetObjectPresignRequest.builder()
                    .signatureDuration(ttl)
                    .getObjectRequest(get)
                    .build();
            return presigner.presignGetObject(preq).url().toString();
        }
    }
}
