package com.ellora.application.feature.admin;

import com.ellora.application.common.port.ImageStorage;
import com.ellora.domain.constant.Role;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.User;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.domain.repository.UserRepository;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApproveSalonUseCase {

    private final SalonRepository salonRepository;
    private final UserRepository userRepository;
    private final ImageStorage imageStorage;

    @Transactional
    public void execute(Long salonId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        salon.approve();
        salonRepository.save(salon);

        promoteOwner(salon);
    }

    @Transactional
    public void execute(Long salonId, String filename, InputStream inputStream, long contentLength, String contentType) {
        if (contentLength <= 0 || inputStream == null) {
            throw new BusinessRuleException("CONTRACT_REQUIRED", "Bắt buộc phải đính kèm tệp hợp đồng khi duyệt salon.");
        }
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon"));

        String folder = "ellora/contracts/" + salonId;
        String contractUrl = imageStorage.uploadContractFile(folder, filename, inputStream, contentLength, contentType);
        String fileType = (contentType != null && contentType.toLowerCase().contains("pdf"))
                || (filename != null && filename.toLowerCase().endsWith(".pdf"))
                ? "PDF" : "IMAGE";

        salon.approveWithContract(contractUrl, filename, fileType);
        salonRepository.save(salon);

        promoteOwner(salon);
    }

    private void promoteOwner(Salon salon) {
        if (salon.getOwnerId() != null) {
            userRepository.findById(salon.getOwnerId()).ifPresent(owner -> {
                if (owner.getRole() == Role.CUSTOMER) {
                    owner.setRole(Role.SALON_OWNER);
                    userRepository.save(owner);
                }
            });
        }
    }
}
