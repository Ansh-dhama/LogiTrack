package LogiTrack.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    // Use this reusable method for everything to handle errors safely
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);

            javaMailSender.send(mail);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            // This prevents the backend from crashing if email fails
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // Refactored to use the safe method above
    @Async // <--- ADD THIS so the user doesn't wait for email to send
    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "LogiTrack - Your OTP Code";
        String body = "Hello,\n\nYour OTP for verification is: " + otp + "\n\nThis code expires in 5 minutes.";

        // Call the safe method inside
        sendEmail(toEmail, subject, body);
    }
}