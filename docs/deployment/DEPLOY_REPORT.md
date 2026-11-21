# DEPLOY REPORT — ellora-booking-be

Ghi sau mỗi phase và mỗi lần deploy. Không chứa secret.

## Mẫu một lần deploy

### \<YYYY-MM-DD HH:MM\> — \<env\>

- Commit:
- Tag (nếu có):
- Người thực hiện:
- Người approve:
- Bắt đầu / kết thúc:

**Image digest**

```text
api=ghcr.io/eddie0517/ellora-api@sha256:...
```

**Digest trước đó (rollback)**

```text
api=ghcr.io/eddie0517/ellora-api@sha256:...
```

**Lệnh đã chạy**

```bash
# chỉ các lệnh thay đổi trạng thái
```

**Kết quả smoke**

| Lớp | Kết quả | Ghi chú |
|---|---|---|
| Container | | |
| Health nội bộ | | |
| Public HTTPS (tunnel) | | |
| CORS | | |
| Migration (Flyway) | | |
| Tài nguyên (`free -h`, `df -h`, `docker stats`) | | |
| Stack khác không đổi | | |

**Sai lệch / xử lý**

-

**Việc còn treo**

-

---

## Mẫu sự cố

### \<YYYY-MM-DD HH:MM\> — \<env\>

- Triệu chứng:
- Phạm vi ảnh hưởng:
- Phát hiện bởi:
- Nguyên nhân gốc:
- Hành động khắc phục:
- Rollback (nếu có):
- Thời gian phục hồi:
- Phòng ngừa lần sau:
