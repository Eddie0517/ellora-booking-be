package com.ellora.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ellora.application.common.port.ImageStorage;
import com.ellora.application.feature.media.UploadImageUseCase;
import com.ellora.application.feature.media.dto.UploadImageResponse;
import com.ellora.domain.exception.BusinessRuleException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediaUseCaseTest {

    @Mock
    private ImageStorage imageStorage;

    private UploadImageUseCase uploadImageUseCase;

    @BeforeEach
    void setUp() {
        uploadImageUseCase = new UploadImageUseCase(imageStorage);
    }

    @Test
    @DisplayName("UploadImage: Success returns image secure URL")
    void uploadImage_success() {
        InputStream stream = new ByteArrayInputStream("fake-image".getBytes());
        when(imageStorage.uploadImage("ellora", "test.jpg", stream, 10, "image/jpeg"))
                .thenReturn("https://res.cloudinary.com/ellora/image/upload/v1/test.jpg");

        UploadImageResponse response = uploadImageUseCase.execute("ellora", "test.jpg", stream, 10, "image/jpeg");

        assertThat(response).isNotNull();
        assertThat(response.url()).isEqualTo("https://res.cloudinary.com/ellora/image/upload/v1/test.jpg");
    }

    @Test
    @DisplayName("UploadImage: Fails when inputStream is null")
    void uploadImage_nullStream_throwsException() {
        assertThatThrownBy(() -> uploadImageUseCase.execute("ellora", "test.jpg", null, 0, "image/jpeg"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Tệp tin hình ảnh không được để trống");
    }
}
