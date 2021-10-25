package br.com.felipe.gadelha.webflux.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.userdetails.User;
import br.com.felipe.gadelha.webflux.domain.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "roles";

    @Value("${spring-webflux.jwt.expiration}")
    private String expiration;

    @Value("${spring-webflux.jwt.secret}")
    private String secret;

    private Date expirationDate() {
        return Date.from(LocalDateTime.now()
                .plusMinutes(Long.parseLong(expiration))
                .atZone(ZoneId.of("America/Sao_Paulo"))
                .toInstant());
    }

    public String generateToken(Authentication authentication) {
        User logged = (User) authentication.getPrincipal();
        var now = new Date();
//		var expirationDate = new Date(now.getTime() + Long.parseLong(expiration));

        String name = logged.getName();
        Collection<? extends GrantedAuthority> authorities = logged.getAuthorities();
        Claims claims = Jwts.claims().setSubject(name);
        claims.put(AUTHORITIES_KEY, authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));

        return Jwts.builder()
                .setIssuer("API Spring-webflux")
//                .setAudience(logged.getName())
//                .setSubject(logged.getId().toString())
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(this.expirationDate())// expirationDate
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList(claims.get(AUTHORITIES_KEY).toString());
        var principal = new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(this.secret)
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
