package app.vaazar.security;

import app.vaazar.domain.i18n.SupportedLanguage;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component
public class JwtTokenUtil {

    private final String jwtSecret;
    private final String jwtIssuer = "sniffas";
    private static final String ROLES_KEY = "roles";
    private static final String SEPARATOR = "::";
    public static final long EXPIRATION_IN_SECONDS = 15 * 60; // 15 minutes
    private final Logger logger;

    public JwtTokenUtil(@Value("${app.jwt-secret}") String jwtSecret, Logger logger) {
        this.jwtSecret = jwtSecret;
        this.logger = logger;
    }

    @PostConstruct
    public void tokenUtilPostConstruct() {
        logger.info("constructed token util with secret: {} ", this.jwtSecret);
    }

    public String generateAccessToken(User user) {
        logger.info("generate token for {}-{}", user.getId(), user.getEmail());
        if (user.getDeleted())
            throw new IllegalStateException("user is deleted");
        String sb = user.getId() + SEPARATOR +
                user.getUsername() + SEPARATOR +
                user.getEmail() + SEPARATOR +
                user.getLanguage();
        Claims claims = Jwts.claims().setSubject(sb);
        claims.put(ROLES_KEY, user.getAuthorities());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_IN_SECONDS * 1000))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public User getAsUser(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        User user = new User();
        try {
            List<SimpleGrantedAuthority> authorities = getRoles(token);
            user.setRole(Role.valueOf(authorities.get(0).getAuthority()));
        } catch (Exception ignore) {
        }
        String[] embeddedInfo = claims.getSubject().split(SEPARATOR);
        user.setId(Long.parseLong(embeddedInfo[0]));
        user.setUsername(embeddedInfo[1]);
        user.setEmail(embeddedInfo[2]);
        user.setLanguage(SupportedLanguage.valueOf(embeddedInfo[3]));
        return user;
    }

    public List<SimpleGrantedAuthority> getRoles(String token) {
        List<Map<String, String>> roleClaims = Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).getBody().get(ROLES_KEY, List.class);
        return roleClaims.stream().map(roleClaim ->
                        new SimpleGrantedAuthority(roleClaim.get("authority")))
                .collect(Collectors.toList());
    }

    public String generateUid(String userMail) {
        logger.info("generate uid for {}", userMail);
        return Jwts.builder()
                .setSubject(format("%s", userMail))
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_IN_SECONDS * 1000))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public boolean isSigned(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).isSigned(token);
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature - {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token - {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token - {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token - {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty - {}", ex.getMessage());
        }
        return false;
    }

}
