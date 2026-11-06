package com.ellora.infrastructure.adapter.mail;

import com.ellora.application.common.port.MailSender;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleSmtpMailSender implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(GoogleSmtpMailSender.class);

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from:${spring.mail.username:no-reply@ellora.vn}}")
    private String fromEmail;

    @Override
    public void sendText(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            log.info("Sent plain text email to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send plain text email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            StringBuilder html = new StringBuilder();
            html.append("<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>");
            html.append("<h2 style='color: #4A154B;'>Ellora Booking</h2>");
            if (variables != null && variables.containsKey("message")) {
                html.append("<p>").append(variables.get("message")).append("</p>");
            }
            if (variables != null && variables.containsKey("code")) {
                html.append("<div style='background: #f4f4f4; padding: 15px; font-size: 24px; font-weight: bold; letter-spacing: 4px; text-align: center; border-radius: 8px;'>")
                    .append(variables.get("code"))
                    .append("</div>");
            }
            html.append("<p style='font-size: 12px; color: #777; margin-top: 30px;'>Nếu bạn không yêu cầu email này, vui lòng bỏ qua.</p>");
            html.append("</div>");

            helper.setText(html.toString(), true);
            javaMailSender.send(message);
            log.info("Sent HTML email to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
        }
    }
}
