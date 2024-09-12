package io.getarrayus.securecapita.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.getarrayus.securecapita.dto.UserPrincipal;
import io.getarrayus.securecapita.dto.UserResponse;
import io.getarrayus.securecapita.exception.BlogAPIException;
import io.getarrayus.securecapita.payload.RolesDto;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.service.RolesService;
import io.getarrayus.securecapita.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static io.getarrayus.securecapita.constant.Constants.*;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app-jwt-expiration-milliseconds}")
    private long jwtExpirationDate; // 1 hour (for access token)

    @Value("${app-jwt-expiration-milliseconds_refresh}")
    private long refreshTokenExpirationDate; // 1 day (for refresh token)

    private final UserService userService;


    // Method to create the Access Token
    public String createAccessToken(UserPrincipal userPrincipal) {
        return JWT.create().withIssuer(GET_ARRAYS_LLC).withAudience(CUSTOMER_MANAGEMENT_SERVICE)
                .withIssuedAt(new Date()).withSubject(String.valueOf(userPrincipal.getUser().getId())).withArrayClaim(AUTHORITIES, getClaimsFromUser(userPrincipal))
                .withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .sign(HMAC512(jwtSecret.getBytes()));

    }

    public String createRefreshToken(UserPrincipal userPrincipal) {
        return JWT.create().withIssuer(GET_ARRAYS_LLC).withAudience(CUSTOMER_MANAGEMENT_SERVICE)
                .withIssuedAt(new Date()).withSubject(String.valueOf(userPrincipal.getUser().getId()))
                .withExpiresAt(new Date(currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .sign(HMAC512(jwtSecret.getBytes()));

    }

    public Long getSubject(String token, HttpServletRequest request) {
        try {
            return Long.valueOf(getJWTVerifier().verify(token).getSubject());
        } catch (TokenExpiredException exception) {
            request.setAttribute("expiredMessage", exception.getMessage());
            throw exception;
        } catch (InvalidClaimException exception) {
            request.setAttribute("invalidClaim", exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            throw exception;
        }
    }

    public Authentication getAuthentication(Long userId, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken userPasswordAuthToken = new UsernamePasswordAuthenticationToken(userService.getUserById(userId), null, authorities);
        userPasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return userPasswordAuthToken;
    }


    public List<GrantedAuthority>getAuthorities(String token) {
        String[] claims = getClaimsFromToken(token);
        return stream(claims).map(SimpleGrantedAuthority::new).collect(toList());
    }

    public boolean isTokenValid(Long userId, String token) {
        JWTVerifier verifier = getJWTVerifier();
        return !Objects.isNull(userId) && !isTokenExpired(verifier, token);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = HMAC512(jwtSecret);
            verifier = JWT.require(algorithm).withIssuer(GET_ARRAYS_LLC).build();
        }catch (JWTVerificationException exception) { throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED); }
        return verifier;
    }

//    public boolean validateToken(String token){
//        try{
//            Jwts.parserBuilder()
//                    .setSigningKey(key())
//                    .build()
//                    .parse(token);
//            return true;
//        } catch (MalformedJwtException ex) {
//            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Invalid JWT token");
//        } catch (ExpiredJwtException ex) {
//            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Expired JWT token");
//        } catch (UnsupportedJwtException ex) {
//            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Unsupported JWT token");
//        } catch (IllegalArgumentException ex) {
//            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "JWT claims string is empty.");
//        }
//    }
}