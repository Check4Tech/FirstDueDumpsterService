package check4tech.firstdueresponseservice.security;

import check4tech.firstdueresponseservice.Entity.Employee;
import check4tech.firstdueresponseservice.Respository.EmployeeRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/** Reads the Bearer token, re-checks the employee in the database (so terminations apply immediately) and signs them in. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final EmployeeRepository employeeRepository;

    public JwtAuthFilter(JwtService jwtService, EmployeeRepository employeeRepository) {
        this.jwtService = jwtService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Optional<Claims> claims = jwtService.verify(header.substring(7));
            if (claims.isPresent()) {
                Optional<Employee> employee = employeeRepository.findByUsername(claims.get().getSubject());
                if (employee.isPresent() && isCurrent(employee.get(), claims.get())) {
                    Employee e = employee.get();
                    AuthenticatedEmployee principal = new AuthenticatedEmployee(
                            e.getId(), e.getUsername(), e.getLocation().getId(), e.getRole());
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + e.getRole().name()))));
                }
            }
        }
        chain.doFilter(request, response);
    }

    private static boolean isCurrent(Employee e, Claims claims) {
        return !Boolean.TRUE.equals(e.getIsTerminated())
                && e.getLocation() != null
                && e.getRole() != null
                && Long.valueOf(e.getLocation().getId()).equals(claims.get("loc", Long.class));
    }
}
