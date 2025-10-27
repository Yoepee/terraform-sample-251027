package com.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class HomeController {
    private final S3Client s3Client;
    private static final String BUCKET_NAME = "dev-bucket-sik2-1";
    private static final String REGION = "ap-northeast-2";
    private static final String IMG_DIR_NAME = "img1";

    public static String getS3FileUrl(String fileName) {
        return "https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/" + fileName;
    }

    @GetMapping("/")
    public List<String> home() {
        List<Bucket> bucketList = s3Client.listBuckets().buckets();
        return bucketList.stream().map(Bucket::name).collect(Collectors.toList());
    }

    @GetMapping("/upload")
    public String upload() {
        return """
                <form action="/upload" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" accept="image/*">
                    <input type="submit" value="Upload">
                </form>
                """;
    }

    @PostMapping("/upload")
    @ResponseBody
    public String handleFileUpload(MultipartFile file) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(IMG_DIR_NAME + "/" + file.getOriginalFilename())
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return "Uploaded Success";
    }
}
