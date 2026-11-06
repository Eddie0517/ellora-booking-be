package com.ellora.application.feature.billing;

import com.ellora.application.common.port.ImageStorage;
import com.ellora.application.feature.billing.dto.BillingQrResponse;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.repository.PlatformBillingSettingsRepository;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManageBillingQrUseCase {
    private static final String BILLING_QR_FOLDER = "platform/billing";

    private final ImageStorage imageStorage;
    private final PlatformBillingSettingsRepository billingSettings;

    public BillingQrResponse get() {
        return new BillingQrResponse(billingSettings.findQrUrl().orElse(null));
    }

    @Transactional
    public BillingQrResponse upload(String filename, InputStream inputStream, long contentLength, String contentType) {
        if (inputStream == null || contentLength <= 0) {
            throw new BusinessRuleException("INVALID_FILE", "Vui lòng chọn tệp tin hình ảnh");
        }
        String url = imageStorage.uploadImage(BILLING_QR_FOLDER, filename, inputStream, contentLength, contentType);
        billingSettings.updateQrUrl(url);
        return new BillingQrResponse(url);
    }
}
