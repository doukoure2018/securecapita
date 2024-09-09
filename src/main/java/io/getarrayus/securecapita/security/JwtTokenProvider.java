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
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app-jwt-expiration-milliseconds}")
    private long jwtExpirationDate; // 1 hour (for access token)

    @Value("${app-jwt-expiration-milliseconds_refresh}")
    private long refreshTokenExpirationDate; // 1 day (for refresh token)

    private SecretKey key() {
        try {
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            byte[] keyBytes = sha512.digest("billetterieguinee".getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes); // Generates a key suitable for HS512
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate key", e);
        }
    }

    // Method to create the Access Token
    // Method to create the Access Token using UserPrincipal
    public String createAccessToken(UserPrincipal userPrincipal) {
        Date currentDate = new Date();
        Date accessTokenExpireDate = new Date(currentDate.getTime() + jwtExpirationDate); // Expiration time for access token

        // Build and sign the access token
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())  // Set subject to user's email or username
                .claim("id", userPrincipal.getUser().getId())  // Add user ID as a claim
                .claim("authorities", userPrincipal.getAuthorities())  // Add authorities as a claim
                .setIssuedAt(currentDate)  // Set the issue date
                .setExpiration(accessTokenExpireDate)  // Set expiration date for access token
                .signWith(key(), SignatureAlgorithm.HS512)  // Sign the token with the secure key
                .compact();
    }

    // Method to create the Refresh Token using UserPrincipal
    public String createRefreshToken(UserPrincipal userPrincipal) {
        Date currentDate = new Date();
        Date refreshTokenExpireDate = new Date(currentDate.getTime() + refreshTokenExpirationDate); // Expiration time for refresh token

        // Build and sign the refresh token
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())  // Set subject to user's email or username
                .claim("id", userPrincipal.getUser().getId())  // Add user ID as a claim
                .setIssuedAt(currentDate)  // Set the issue date
                .setExpiration(refreshTokenExpireDate)  // Set expiration date for refresh token
                .signWith(key(), SignatureAlgorithm.HS512)  // Sign the token with the secure key
                .compact();
    }


//    private Key key(){
//        return Keys.hmacShaKeyFor(
//                Decoders.BASE64.decode(jwtSecret)
//        );
//    }

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

    public String getUsername(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        String username = claims.getSubject();
        return username;
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

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException ex) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "JWT claims string is empty.");
        }
    }
}