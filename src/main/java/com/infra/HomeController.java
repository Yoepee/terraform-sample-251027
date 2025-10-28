package com.infra;

import com.infra.aws.s3.S3PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class HomeController {
    private final S3Client s3Client;
    private final HomeService homeService;
    private final S3PresignService presignService;
    private static final String BUCKET_NAME = "dev-bucket-dykim-1";
    private static final String REGION = "ap-northeast-2";
    private static final String IMG_DIR_NAME = "img1";

    public static String getS3FileUrl(String fileName) {
        return "https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/" + fileName;
    }

    @GetMapping("/")
    public String home() {
        List<String> keys = homeService.listImageKeys(BUCKET_NAME, IMG_DIR_NAME); // List<String>

        String itemsHtml = keys.stream()
                .map(k -> {
                    var url = presignService.presignedGetUrl(BUCKET_NAME, k, Duration.ofMinutes(10));
                    var alt = HtmlUtils.htmlEscape(k);
                    return """
                <li class="item">
                  <a href="%s" target="_blank" rel="noopener">
                    <img src="%s" alt="%s"/>
                  </a>
                  <div class="caption">%s</div>
                </li>
                """.formatted(url, url, alt, alt);
                })
                .collect(Collectors.joining("\n"));

        // 🔴 주의: CSS의 %들은 전부 %% 로
        String template = """
        <!doctype html>
        <html lang="ko">
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1"/>
          <title>S3 파일 업로드 테스트2</title>
          <style>
            body { margin: 24px; }
            .grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap:16px; }
            img { width:100%%; height:180px; object-fit:cover; border-radius:8px; display:block; }
          </style>
        </head>
        <body>
          <div class="top">
            <h1>S3 파일 업로드 테스트</h1>
            <a href="/upload">파일 업로드</a>
          </div>
          %s
        </body>
        </html>
        """;

        String body = keys.isEmpty()
                ? "<p>표시할 이미지가 없습니다.</p>"
                : "<ul class=\"grid\">" + itemsHtml + "</ul>";

        return template.formatted(body);
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

        return """
                <img src="%s">
                <hr>
                <div>업로드 완료</div>
                """.formatted(getS3FileUrl(IMG_DIR_NAME + "/" + file.getOriginalFilename()));
    }

    @GetMapping("/deleteFile")
    public String showDeleteFile() {
        return """
                <form action="/deleteFile" method="post">
                    <input type="text" name="fileName">
                    <input type="submit" value="delete">
                </form>
                """;
    }

    @PostMapping("/deleteFile")
    @ResponseBody
    public String deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(IMG_DIR_NAME + "/" + fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        return "파일이 삭제되었습니다.";
    }
}
