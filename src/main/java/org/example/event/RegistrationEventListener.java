package org.example.event;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.Entity.User;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationEventListener implements ApplicationListener<RegistrationEvent> {
    private static final long JWT_EXPIRATION_TIME = 1800000L;
    private static final String  JWT_SECRET="rb8suVWT1XpgdOAdzY7b9HLuog0kYik3m9ELPBp+Cp2JwLk2vVEHvvVIEH1rcnZFZ7RRQVmDIQbsVT4VWyfSNKxlmnh8sMpKJGFEsauquNLMy7wyM24QOMM6wbQBWHzBPO1QWw5AH2kkwYEtr5C8jfsH27ajHXkporrqYCLQ2oIsjUwdCthEMlVyhmnHi78b3G+rH+shsN8P+t523D40XmsNODoRsgWvaEU4Dr9d4jnZ2jOCilL2eOYyfUOF7irmIkX+IsOnvNAOAG5bwl1/tgXpWbceIO7XYyABrQ5u5gT+8gMbZBcaQdDExvB7/KoW049b1t0MGDiQnibIeJNlsQ==";
    private static final Logger log = LoggerFactory.getLogger(RegistrationEventListener.class);

    private final UserService userService;

    private final JavaMailSender mailSender;
    private User user;
    @Override
    public void onApplicationEvent(RegistrationEvent event) {

        user = event.getUser();
        Map<String, Object> claims = Map.of();
        String verificationToken = createJwtToken(user.getEmail(),claims);

        userService.saveUserVerificationToken(user, verificationToken);

        String baseUrl = event.getApplicationUrl();
        // Remove any trailing colon or extra characters
        if (baseUrl.endsWith(":")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String url = baseUrl+"/register/verifyEmail?token="+verificationToken;
        try {
            sendVerificationEmail(url);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("click the link to verify your registration: {}", url);
    }

    public static String createJwtToken(String email, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private static Key getSignKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Email Verification";
        String senderName ="User Registration Portal Service";
        String mailContent = "<p> Hi, "+ user.getFirstName()+ "</p>"+
                "<p>Thank you for registering with us,Please, follow the link below to complete your registration.</p>" +
                "<a href=\"" + url + "\">Verify your email to activate your account</a>"+
                "<p> Thank you <br> Users Registration Portal Service</p>";
        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper =new MimeMessageHelper(message);
        messageHelper.setFrom("rayson.khoo@beans.com.my", senderName);
        messageHelper.setTo(user.getEmail());
        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        mailSender.send(message);

    }
}
