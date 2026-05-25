package com.tvscs.bgv.service.impl;

import com.tvscs.bgv.config.AppProperties;
import com.tvscs.bgv.domain.entity.Appeal;
import com.tvscs.bgv.domain.entity.Verifier;
import com.tvscs.bgv.repository.AdminRepository;
import com.tvscs.bgv.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final AdminRepository adminRepository;
    private final AppProperties appProperties;

    public EmailServiceImpl(JavaMailSender mailSender, AdminRepository adminRepository, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.adminRepository = adminRepository;
        this.appProperties = appProperties;
    }

    @Override
    public void sendAppealNotification(Appeal appeal, Verifier verifier) {
        String to = getAdminRecipients();
        if (to == null) return;

        String subject = "New Query Submitted - Employee " + appeal.getEmployeeId();
        String html = """
                <!DOCTYPE html><html><head><meta charset="utf-8">
                <style>body{font-family:Arial,sans-serif;line-height:1.6;color:#333}
                .container{max-width:600px;margin:0 auto;padding:20px}
                .header{background:#dc3545;color:white;padding:20px;text-align:center}
                .content{padding:20px;background:#f9f9f9}
                .alert{background:#f8d7da;border:1px solid #f5c6cb;color:#721c24;padding:15px;border-radius:5px;margin:20px 0}
                .details{background:#fff;padding:15px;border:1px solid #ddd;border-radius:5px;margin:10px 0}
                .footer{padding:20px;text-align:center;font-size:12px;color:#666}</style></head>
                <body><div class="container">
                <div class="header"><h1>New Query Submitted</h1></div>
                <div class="content">
                <div class="alert"><strong>Action Required:</strong> A new query has been submitted that requires your review.</div>
                <p>Dear Admin,</p>
                <p>A new query has been submitted by <strong>%s</strong> regarding employee <strong>%s</strong>.</p>
                <h3>Query Details</h3>
                <div class="details">
                <p><strong>Query ID:</strong> %s</p>
                <p><strong>Employee ID:</strong> %s</p>
                <p><strong>Verifier:</strong> %s (%s)</p>
                <p><strong>Status:</strong> <span style="color:#ffc107;">PENDING</span></p>
                </div>
                <h3>Verifier Comments</h3>
                <div class="details">%s</div>
                <p>Please review at your earliest convenience.</p>
                <p>Best regards,<br>BGV Portal System</p>
                </div>
                <div class="footer"><p>This is an automated message. Please do not reply.</p></div>
                </div></body></html>
                """.formatted(
                verifier != null ? verifier.getCompanyName() : "Unknown",
                appeal.getEmployeeId(),
                appeal.getAppealId(),
                appeal.getEmployeeId(),
                verifier != null ? verifier.getCompanyName() : "Unknown",
                verifier != null ? verifier.getEmail() : "N/A",
                appeal.getAppealReason() != null ? appeal.getAppealReason() : "No comments"
        );

        sendEmail(to, subject, html);
    }

    @Override
    public void sendBlockNotification(Verifier verifier, String employeeId, int attemptCount) {
        String to = getAdminRecipients();
        if (to == null) return;

        String subject = "Verifier Blocked - " + (verifier != null ? verifier.getCompanyName() : "Unknown");
        String html = """
                <!DOCTYPE html><html><head><meta charset="utf-8">
                <style>body{font-family:Arial,sans-serif;line-height:1.6;color:#333}
                .container{max-width:600px;margin:0 auto;padding:20px}
                .header{background:#dc3545;color:white;padding:20px;text-align:center}
                .content{padding:20px;background:#f9f9f9}
                .alert{background:#f8d7da;border:1px solid #f5c6cb;color:#721c24;padding:15px;border-radius:5px;margin:20px 0}
                .details{background:#fff;padding:15px;border:1px solid #ddd;border-radius:5px;margin:10px 0}
                .footer{padding:20px;text-align:center;font-size:12px;color:#666}</style></head>
                <body><div class="container">
                <div class="header"><h1>Verifier Blocked</h1></div>
                <div class="content">
                <div class="alert"><strong>Alert:</strong> A verifier has been blocked due to multiple failed verification attempts.</div>
                <p>Dear Admin,</p>
                <p>A verifier has been blocked from performing employee verifications.</p>
                <h3>Block Details</h3>
                <div class="details">
                <p><strong>Verifier:</strong> %s</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Employee ID Attempted:</strong> %s</p>
                <p><strong>Attempt Count:</strong> %d</p>
                </div>
                <p>Best regards,<br>BGV Portal System</p>
                </div>
                <div class="footer"><p>This is an automated message.</p></div>
                </div></body></html>
                """.formatted(
                verifier != null ? verifier.getCompanyName() : "Unknown",
                verifier != null ? verifier.getEmail() : "N/A",
                employeeId,
                attemptCount
        );

        sendEmail(to, subject, html);
    }

    @Override
    public void sendAppealResponse(Appeal appeal, String verifierEmail) {
        String subject = "Response to Your Query - Employee " + appeal.getEmployeeId();
        String html = """
                <!DOCTYPE html><html><head><meta charset="utf-8">
                <style>body{font-family:Arial,sans-serif;line-height:1.6;color:#333}
                .container{max-width:600px;margin:0 auto;padding:20px}
                .header{background:#007A3D;color:white;padding:20px;text-align:center}
                .content{padding:20px;background:#f9f9f9}
                .status{background:#28a745;color:white;padding:10px;text-align:center;border-radius:5px;font-weight:bold;margin:20px 0}
                .response{background:#fff;padding:15px;border:1px solid #ddd;border-radius:5px;margin:20px 0}
                .details{background:#fff;padding:15px;border:1px solid #ddd;border-radius:5px;margin:10px 0}
                .footer{padding:20px;text-align:center;font-size:12px;color:#666}</style></head>
                <body><div class="container">
                <div class="header"><h1>Query Response</h1></div>
                <div class="content">
                <p>Dear Verifier,</p>
                <p>Your query regarding employee <strong>%s</strong> has been reviewed by our HR team.</p>
                <div class="status">Query Status: Completed</div>
                <h3>HR Response</h3>
                <div class="response">%s</div>
                <h3>Query Details</h3>
                <div class="details">
                <p><strong>Query ID:</strong> %s</p>
                <p><strong>Employee ID:</strong> %s</p>
                </div>
                <p>Best regards,<br>HR Team<br>BGV Portal</p>
                </div>
                <div class="footer"><p>This is an automated message. Please do not reply.</p></div>
                </div></body></html>
                """.formatted(
                appeal.getEmployeeId(),
                appeal.getHrResponse() != null ? appeal.getHrResponse() : "",
                appeal.getAppealId(),
                appeal.getEmployeeId()
        );

        sendEmail(verifierEmail, subject, html);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(appProperties.getMail().getFrom());
            helper.setTo(to.split(","));
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {} — {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String getAdminRecipients() {
        List<String> emails = adminRepository.findAllActiveAdminEmails();
        if (!emails.isEmpty()) return String.join(",", emails);
        String fallback = appProperties.getAdminEmail();
        if (fallback == null || fallback.isBlank()) {
            log.warn("No admin email recipients configured");
            return null;
        }
        return fallback;
    }
}
