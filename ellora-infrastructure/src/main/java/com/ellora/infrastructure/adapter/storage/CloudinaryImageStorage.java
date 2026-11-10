package com.ellora.infrastructure.adapter.storage;

import com.ellora.application.common.port.ImageStorage;
import com.ellora.domain.exception.BusinessRuleException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CloudinaryImageStorage implements ImageStorage {

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CloudinaryImageStorage(
            @Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:}") String apiSecret,
            ObjectMapper objectMapper) {
        this.cloudName = cloudName != null ? cloudName.trim() : "";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiSecret = apiSecret != null ? apiSecret.trim() : "";
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String uploadImage(String folder, String filename, InputStream inputStream, long contentLength, String contentType) {
        if (contentLength <= 0 || contentLength > 10 * 1024 * 1024) {
            throw new BusinessRuleException("INVALID_IMAGE_SIZE", "Kích thước ảnh phải lớn hơn 0 và không vượt quá 10 MB.");
        }

        byte[] bytes;
        try {
            bytes = inputStream.readAllBytes();
            validateImageBytes(bytes);
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse image bytes", e);
            throw new BusinessRuleException("INVALID_IMAGE", "Tệp tin hình ảnh không hợp lệ hoặc bị hỏng.");
        }

        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            log.warn("Cloudinary is not configured on this environment. Fallback to placeholder image.");
            return "https://res.cloudinary.com/ellora/image/upload/v1/samples/mock-" + System.currentTimeMillis() + ".jpg";
        }

        String boundary = "----ElloraBoundary" + UUID.randomUUID().toString().replace("-", "");
        byte[] multipartBody = buildMultipartBody(boundary, folder, filename, bytes, contentType);

        String credentials = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8));
        String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .timeout(Duration.ofSeconds(45))
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("secure_url")) {
                    return root.get("secure_url").asText();
                }
            }
            log.error("Cloudinary response failed with code {}: {}", response.statusCode(), response.body());
            throw new BusinessRuleException("UPLOAD_FAILED", "Không thể tải ảnh lên dịch vụ Cloudinary. Vui lòng thử lại sau.");
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            log.error("Cloudinary upload request exception", e);
            throw new BusinessRuleException("UPLOAD_FAILED", "Không thể tải ảnh lên dịch vụ Cloudinary. Vui lòng thử lại sau.");
        }
    }

    @Override
    public String uploadContractFile(String folder, String filename, InputStream inputStream, long contentLength, String contentType) {
        if (contentLength <= 0 || contentLength > 15 * 1024 * 1024) {
            throw new BusinessRuleException("INVALID_FILE_SIZE", "Kích thước tài liệu hợp đồng không được vượt quá 15 MB.");
        }

        byte[] bytes;
        try {
            bytes = inputStream.readAllBytes();
        } catch (Exception e) {
            throw new BusinessRuleException("INVALID_FILE", "Không thể đọc dữ liệu tệp hợp đồng.");
        }

        boolean isPdf = (bytes.length >= 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F')
                || (contentType != null && contentType.toLowerCase().contains("pdf"));

        if (!isPdf) {
            // Must be valid image
            validateImageBytes(bytes);
        }

        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            log.warn("Cloudinary is not configured on this environment. Fallback to mock contract URL.");
            return "https://res.cloudinary.com/ellora/raw/upload/v1/contracts/mock-" + System.currentTimeMillis() + (isPdf ? ".pdf" : ".jpg");
        }

        String boundary = "----ElloraBoundary" + UUID.randomUUID().toString().replace("-", "");
        byte[] multipartBody = buildMultipartBody(boundary, folder, filename, bytes, isPdf ? "application/pdf" : contentType);

        String credentials = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8));
        String endpointType = isPdf ? "auto" : "image";
        String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/" + endpointType + "/upload";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("secure_url")) {
                    return root.get("secure_url").asText();
                }
            }
            log.error("Cloudinary contract upload response failed with code {}: {}", response.statusCode(), response.body());
            throw new BusinessRuleException("UPLOAD_FAILED", "Không thể tải tài liệu hợp đồng lên dịch vụ lưu trữ.");
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            log.error("Cloudinary contract upload request exception", e);
            throw new BusinessRuleException("UPLOAD_FAILED", "Không thể tải tài liệu hợp đồng lên dịch vụ lưu trữ.");
        }
    }

    private byte[] buildMultipartBody(String boundary, String folder, String filename, byte[] fileBytes, String contentType) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            String targetFolder = (folder != null && !folder.isBlank()) ? folder : "ellora";
            String targetFile = (filename != null && !filename.isBlank()) ? filename : "image.jpg";
            String targetType = (contentType != null && !contentType.isBlank()) ? contentType : "image/jpeg";

            // folder part
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write("Content-Disposition: form-data; name=\"folder\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            baos.write((targetFolder + "\r\n").getBytes(StandardCharsets.UTF_8));

            // file part
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + targetFile + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Type: " + targetType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(fileBytes);
            baos.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // final boundary
            baos.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to construct multipart body", e);
        }
    }

    private void validateImageBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            throw new BusinessRuleException("INVALID_IMAGE", "Tệp tin hình ảnh không hợp lệ hoặc bị rỗng.");
        }

        boolean isJpeg = (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF);
        boolean isPng = (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47
                && bytes[4] == (byte) 0x0D && bytes[5] == (byte) 0x0A && bytes[6] == (byte) 0x1A && bytes[7] == (byte) 0x0A);
        boolean isWebp = (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P');
        boolean isGif = (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8');

        if (!isJpeg && !isPng && !isWebp && !isGif) {
            throw new BusinessRuleException("INVALID_IMAGE_FORMAT", "Chỉ hỗ trợ ảnh định dạng JPG, PNG hoặc WebP.");
        }

        if (isJpeg || isPng || isGif) {
            try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(input);
                if (readers.hasNext()) {
                    var reader = readers.next();
                    try {
                        reader.setInput(input);
                        if ((long) reader.getWidth(0) * reader.getHeight(0) > 40_000_000L) {
                            throw new BusinessRuleException("IMAGE_TOO_LARGE", "Độ phân giải ảnh vượt quá giới hạn tối đa 40 megapixel.");
                        }
                    } finally {
                        reader.dispose();
                    }
                }
            } catch (BusinessRuleException e) {
                throw e;
            } catch (Exception e) {
                log.debug("Image dimension probe note: {}", e.getMessage());
            }
        }
    }

    @Override
    public void deleteImage(String publicUrlOrId) {
        if (publicUrlOrId == null || publicUrlOrId.isBlank()) {
            return;
        }
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            log.info("Cloudinary delete skipped (not configured): {}", publicUrlOrId);
            return;
        }

        try {
            String publicId = publicUrlOrId;
            if (publicUrlOrId.contains("/upload/")) {
                String sub = publicUrlOrId.substring(publicUrlOrId.indexOf("/upload/") + 8);
                if (sub.matches("^v\\d+/.*")) {
                    sub = sub.substring(sub.indexOf('/') + 1);
                }
                int dotIdx = sub.lastIndexOf('.');
                publicId = dotIdx > 0 ? sub.substring(0, dotIdx) : sub;
            }

            String boundary = "----ElloraBoundary" + UUID.randomUUID().toString().replace("-", "");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write("Content-Disposition: form-data; name=\"public_id\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            baos.write((publicId + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

            String credentials = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8));
            String destroyUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(destroyUrl))
                    .header("Authorization", "Basic " + credentials)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            log.info("Successfully requested image deletion from Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary: {}", e.getMessage());
        }
    }
}
