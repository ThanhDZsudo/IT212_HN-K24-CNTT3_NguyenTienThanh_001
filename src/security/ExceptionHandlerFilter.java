package security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, e.getMessage());
        } catch (JwtException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, "Invalid JWT token: " + e.getMessage());
        } catch (RuntimeException e) {
            setErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, response, "Internal server error: " + e.getMessage());
        }
    }

    private void setErrorResponse(HttpStatus status, HttpServletResponse response, String message) {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String escapedMessage = message != null ? message.replace("\"", "\\\"") : "";
            String json = String.format("{\"error\": \"AUTH_FAILED\", \"message\": \"%s\"}", escapedMessage);
            response.getWriter().write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
