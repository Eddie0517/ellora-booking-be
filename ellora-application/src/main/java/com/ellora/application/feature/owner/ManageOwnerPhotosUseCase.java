package com.ellora.application.feature.owner;

import com.ellora.application.common.port.ImageStorage;
import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.model.Employee;
import com.ellora.domain.model.NailService;
import com.ellora.domain.model.Salon;
import com.ellora.domain.model.SalonChange;
import com.ellora.domain.repository.EmployeeRepository;
import com.ellora.domain.repository.NailServiceRepository;
import com.ellora.domain.repository.SalonChangeRepository;
import com.ellora.domain.repository.SalonRepository;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManageOwnerPhotosUseCase {

    private final SalonRepository salonRepository;
    private final NailServiceRepository nailServiceRepository;
    private final EmployeeRepository employeeRepository;
    private final SalonChangeRepository salonChangeRepository;
    private final ImageStorage imageStorage;

    private Salon getOwnedSalon(UUID ownerId) {
        if (ownerId == null) {
            throw new BusinessRuleException("UNAUTHORIZED", "Vui lòng đăng nhập với tài khoản chủ salon");
        }
        List<Salon> salons = salonRepository.findByOwnerId(ownerId);
        if (salons.isEmpty()) {
            throw new BusinessRuleException("SALON_NOT_FOUND", "Không tìm thấy salon của chủ tiệm");
        }
        return salons.get(0);
    }

    @Transactional
    public String uploadPhoto(UUID ownerId, String kind, Long id, String filename, InputStream inputStream, long size, String contentType) {
        Salon salon = getOwnedSalon(ownerId);
        String url = imageStorage.uploadImage("ellora/" + kind, filename, inputStream, size, contentType);

        switch (kind.toLowerCase()) {
            case "cover" -> {
                if (!salon.getId().equals(id)) {
                    throw new BusinessRuleException("FORBIDDEN", "Không có quyền chỉnh sửa salon này");
                }
                String previousLogo = salon.getLogoUrl();
                salon.setLogoUrl(url);
                salonRepository.save(salon);

                salonChangeRepository.save(new SalonChange(
                        null,
                        salon.getId(),
                        "COVER",
                        "{}",
                        previousLogo,
                        url,
                        null,
                        null,
                        "APPROVED",
                        Instant.now(),
                        ownerId,
                        Instant.now()
                ));
                return url;
            }
            case "services" -> {
                NailService service = nailServiceRepository.findById(id)
                        .orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
                if (!salon.getId().equals(service.getSalonId())) {
                    throw new BusinessRuleException("FORBIDDEN", "Dịch vụ không thuộc salon của bạn");
                }
                service.setImageUrl(url);
                nailServiceRepository.save(service);
                return url;
            }
            case "employees" -> {
                Employee employee = employeeRepository.findById(id)
                        .orElseThrow(() -> new BusinessRuleException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
                if (!salon.getId().equals(employee.getSalonId())) {
                    throw new BusinessRuleException("FORBIDDEN", "Nhân viên không thuộc salon của bạn");
                }
                employee.setAvatarUrl(url);
                employeeRepository.save(employee);
                return url;
            }
            default -> throw new BusinessRuleException("INVALID_KIND", "Loại ảnh không hợp lệ: " + kind);
        }
    }

    @Transactional
    public void removePhoto(UUID ownerId, String kind, Long id) {
        Salon salon = getOwnedSalon(ownerId);
        switch (kind.toLowerCase()) {
            case "cover" -> {
                if (!salon.getId().equals(id)) {
                    throw new BusinessRuleException("FORBIDDEN", "Không có quyền chỉnh sửa salon này");
                }
                String prev = salon.getLogoUrl();
                salon.setLogoUrl(null);
                salonRepository.save(salon);
                salonChangeRepository.save(new SalonChange(
                        null, salon.getId(), "COVER", "{}", prev, null, null, null,
                        "APPROVED", Instant.now(), ownerId, Instant.now()
                ));
            }
            case "services" -> {
                NailService service = nailServiceRepository.findById(id)
                        .orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
                if (!salon.getId().equals(service.getSalonId())) {
                    throw new BusinessRuleException("FORBIDDEN", "Dịch vụ không thuộc salon của bạn");
                }
                service.setImageUrl(null);
                nailServiceRepository.save(service);
            }
            case "employees" -> {
                Employee employee = employeeRepository.findById(id)
                        .orElseThrow(() -> new BusinessRuleException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
                if (!salon.getId().equals(employee.getSalonId())) {
                    throw new BusinessRuleException("FORBIDDEN", "Nhân viên không thuộc salon của bạn");
                }
                employee.setAvatarUrl(null);
                employeeRepository.save(employee);
            }
            default -> throw new BusinessRuleException("INVALID_KIND", "Loại ảnh không hợp lệ: " + kind);
        }
    }

    @Transactional
    public List<String> uploadGallery(UUID ownerId, Long salonId, List<UploadFileInput> files) {
        Salon salon = getOwnedSalon(ownerId);
        if (!salon.getId().equals(salonId)) {
            throw new BusinessRuleException("FORBIDDEN", "Không có quyền chỉnh sửa salon này");
        }
        if (files == null || files.isEmpty()) {
            throw new BusinessRuleException("INVALID_FILE", "Chọn ít nhất một ảnh để tải lên");
        }
        if (salon.getImageUrls().size() + files.size() > 10) {
            throw new BusinessRuleException("MAX_PHOTOS_EXCEEDED", "Mỗi salon chỉ được tải tối đa 10 ảnh thư viện");
        }

        List<String> uploaded = new ArrayList<>();
        List<String> gallery = new ArrayList<>(salon.getImageUrls());
        for (UploadFileInput file : files) {
            String url = imageStorage.uploadImage("ellora/gallery", file.filename(), file.inputStream(), file.size(), file.contentType());
            uploaded.add(url);
            gallery.add(url);
        }

        salon.setImageUrls(gallery);
        salonRepository.save(salon);

        salonChangeRepository.save(new SalonChange(
                null, salon.getId(), "GALLERY", "{}", salon.getLogoUrl(), salon.getLogoUrl(),
                null, null, "APPROVED", Instant.now(), ownerId, Instant.now()
        ));
        return uploaded;
    }

    @Transactional
    public void removeGallery(UUID ownerId, String url) {
        Salon salon = getOwnedSalon(ownerId);
        List<String> gallery = new ArrayList<>(salon.getImageUrls());
        if (!gallery.remove(url)) {
            throw new BusinessRuleException("IMAGE_NOT_FOUND", "Ảnh không còn tồn tại trong bộ sưu tập");
        }
        salon.setImageUrls(gallery);
        salonRepository.save(salon);
    }

    @Transactional
    public List<String> uploadShowcaseSlot(UUID ownerId, int slotIndex, String filename, InputStream inputStream, long size, String contentType) {
        if (slotIndex < 0 || slotIndex >= 5) {
            throw new BusinessRuleException("INVALID_SLOT", "Vị trí ảnh khung showcase chỉ từ 0 đến 4");
        }
        Salon salon = getOwnedSalon(ownerId);
        String url = imageStorage.uploadImage("ellora/showcase", filename, inputStream, size, contentType);

        List<String> list = new ArrayList<>(salon.getImageUrls());
        while (list.size() <= slotIndex) {
            list.add("");
        }
        list.set(slotIndex, url);

        salon.setImageUrls(list);
        salonRepository.save(salon);

        salonChangeRepository.save(new SalonChange(
                null, salon.getId(), "GALLERY", "{}", salon.getLogoUrl(), salon.getLogoUrl(),
                null, null, "APPROVED", Instant.now(), ownerId, Instant.now()
        ));
        return list;
    }

    @Transactional
    public List<String> removeShowcaseSlot(UUID ownerId, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 5) {
            throw new BusinessRuleException("INVALID_SLOT", "Vị trí ảnh khung showcase chỉ từ 0 đến 4");
        }
        Salon salon = getOwnedSalon(ownerId);
        List<String> list = new ArrayList<>(salon.getImageUrls());
        if (slotIndex < list.size()) {
            list.set(slotIndex, "");
            while (!list.isEmpty() && list.get(list.size() - 1).isBlank()) {
                list.remove(list.size() - 1);
            }
            salon.setImageUrls(list);
            salonRepository.save(salon);
        }
        return list;
    }

    public record UploadFileInput(String filename, InputStream inputStream, long size, String contentType) {}
}
