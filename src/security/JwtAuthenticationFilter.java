package security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final String SECRET_KEY = "rikkei_secret_key_super_secure_do_not_share";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            
            // Phân tích và kiểm chứng JWT Token
            String username = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
                    
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Logic set Authentication vào SecurityContext (đã rút gọn theo đề bài)
            }
        }
        filterChain.doFilter(request, response);
    }
}
