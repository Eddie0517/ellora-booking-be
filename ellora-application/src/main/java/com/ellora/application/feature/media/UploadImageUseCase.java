package com.ellora.application.feature.media;

import com.ellora.application.common.port.ImageStorage;
import com.ellora.application.feature.media.dto.UploadImageResponse;
import com.ellora.domain.exception.BusinessRuleException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UploadImageUseCase {

    private final ImageStorage imageStorage;

    public UploadImageResponse execute(String folder, String filename, InputStream inputStream, long contentLength, String contentType) {
        if (inputStream == null) {
            throw new BusinessRuleException("INVALID_FILE", "Tệp tin hình ảnh không được để trống");
        }
        String url = imageStorage.uploadImage(folder, filename, inputStream, contentLength, contentType);
        return new UploadImageResponse(url);
    }
}
