package com.ellora.application.common.port;

import java.util.Map;

/**
 * Port interface for sending emails (transactional, notifications, verification).
 */
public interface MailSender {

    void sendHtml(String to, String subject, String templateName, Map<String, Object> variables);

    void sendText(String to, String subject, String body);
}
