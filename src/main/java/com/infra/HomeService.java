package com.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final S3Client s3;

    public List<String> listImageKeys(String bucket, String prefix) {
        List<String> imageExt = List.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp", ".svg");

        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix == null ? "" : prefix) // "folder/" 형태 권장
                .build();

        // paginator로 대용량도 안전하게
        return s3.listObjectsV2Paginator(req).contents().stream()
                .map(S3Object::key)
                .filter(k -> {
                    String lower = k.toLowerCase();
                    return imageExt.stream().anyMatch(lower::endsWith);
                })
                .toList();
    }
}
