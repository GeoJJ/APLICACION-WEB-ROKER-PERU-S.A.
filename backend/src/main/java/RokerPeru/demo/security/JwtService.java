package RokerPeru.demo.security;

import RokerPeru.demo.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Extrae el correo (username) del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Genera el token solo con los datos del usuario
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Genera el token con "claims" extra (ej. el Rol del usuario para React)
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Valida que el token pertenezca al usuario, no haya expirado y su sesión siga vigente
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token)
                && coincideVersionSesion(token, userDetails);
    }

    // El token solo vale si lleva la misma versión de sesión que el usuario tiene ahora en la base de datos.
    // Al cerrar sesión esa versión sube, así que los tokens anteriores quedan invalidados.
    // Un token sin ese dato (emitido antes de este cambio) no se acepta: hay que volver a iniciar sesión.
    private boolean coincideVersionSesion(String token, UserDetails userDetails) {
        if (!(userDetails instanceof Usuario usuario)) {
            return true;
        }
        Integer version = extractClaim(token, claims -> claims.get("ver", Integer.class));
        return version != null && version == usuario.getVersionSesion();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Decodifica la clave secreta
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}