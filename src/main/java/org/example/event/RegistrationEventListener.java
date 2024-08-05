package org.example.event;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.Entity.User;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

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


    @Override
    public void onApplicationEvent(RegistrationEvent event) {

        User user = event.getUser();
        Map<String, Object> claims = Map.of();
        String verificationToken = createJwtToken(user.getEmail(),claims);

        userService.saveUserVerificationToken(user, verificationToken);


        String url = event.getApplicationUrl()+"/register/verifyEmail?token="+verificationToken;
        log.info("click the link to verify your registration:{}", url);
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
}
