package app.vaazar.Security;

import app.vaazar.Domain.User.Entity.Role;
import app.vaazar.Domain.User.Entity.User;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final String jwtSecret = "letMeIn";
    private final String jwtIssuer = "VideoBazaar";
    private final String ROLES_KEY = "roles";
    private static final String SEPARATOR = "::";

    @Autowired
    Logger logger;

    @PostConstruct
    public void tokenUtilPostConstruct() {
        logger.info("constructed token util : {} ", this);
    }

    public String generateAccessToken(String userMail) {
        logger.info("generate token for {}", userMail);
        return Jwts.builder()
                .setSubject(format("%s", userMail))
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 1 day
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String generateAccessToken(User user) {
        logger.info("generate token for {}", user.getUsername());
        Claims claims = Jwts.claims().setSubject(format("%s%s%s", user.getUsername(), SEPARATOR, user.getId()));
                claims.put(ROLES_KEY, user.getAuthorities());
        return Jwts.builder()
                .setClaims(claims)

                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 1 day
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public User getAsUser(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        User user =  new User();
        try {
            List<SimpleGrantedAuthority> authorities = getRoles(token);
            user.setRole(Role.valueOf(authorities.get(0).getAuthority()));
        } catch (Exception ignore){}

        user.setId(Long.parseLong(claims.getSubject().split(SEPARATOR)[1]));
        user.setUsername(claims.getSubject().split(SEPARATOR)[0]);
        return user;
    }

    public List<SimpleGrantedAuthority> getRoles(String token) {
        List<Map<String, String>>  roleClaims = Jwts.parser().setSigningKey(jwtSecret)
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
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 1 day
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
