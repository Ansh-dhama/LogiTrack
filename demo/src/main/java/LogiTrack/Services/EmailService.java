package LogiTrack.Services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Async
    public void sendEmail(String to, String subject, String body) {

        try {

            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions email =
                    CreateEmailOptions.builder()
                            .from("LogiTrack <onboarding@resend.dev>")
                            .to(to)
                            .subject(subject)
                            .html(
                                    "<div style='font-family:Arial,sans-serif;'>"
                                            + body.replace("\n", "<br>")
                                            + "</div>"
                            )
                            .build();

            log.info("Sending email to: {}", to);

            CreateEmailResponse response =
                    resend.emails().send(email);

            log.info(
                    "Email sent successfully to: {} | Resend ID: {}",
                    to,
                    response.getId()
            );

        } catch (ResendException e) {

            log.error(
                    "Failed to send email to {}: {}",
                    to,
                    e.getMessage(),
                    e
            );
        }
    }
}