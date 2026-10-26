package check4tech.firstdueresponseservice.Service;

import check4tech.firstdueresponseservice.Dto.Request.LoginRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.LoginResponseDto;
import check4tech.firstdueresponseservice.Entity.Employee;
import check4tech.firstdueresponseservice.Respository.EmployeeRepository;
import check4tech.firstdueresponseservice.security.JwtService;
import check4tech.firstdueresponseservice.security.LoginAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@Service
public class AuthService {

    private static final String INVALID = "Invalid credentials.";

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService attempts;
    /** Compared against when the user does not exist, so timing does not reveal valid usernames. */
    private final String dummyHash;

    public AuthService(EmployeeRepository employeeRepository, BCryptPasswordEncoder passwordEncoder,
                       JwtService jwtService, LoginAttemptService attempts) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.attempts = attempts;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    public LoginResponseDto login(Long locationId, LoginRequestDto request) {
        String username = request == null || request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request == null || request.getPassword() == null ? "" : request.getPassword();
        String scope = String.valueOf(locationId);

        if (attempts.isLocked(scope, username)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many failed attempts. Try again in 15 minutes.");
        }

        Optional<Employee> found = employeeRepository.findByUsername(username);
        if (found.isEmpty()) {
            found = employeeRepository.findByUsername(username.toUpperCase());
        }
        Employee employee = found.orElse(null);
        boolean inLocation = employee != null && employee.getLocation() != null
                && Objects.equals(employee.getLocation().getId(), locationId);
        boolean active = employee != null && !Boolean.TRUE.equals(employee.getIsTerminated()) && employee.getRole() != null;

        boolean passwordOk = passwordEncoder.matches(password, inLocation && employee.getPassword() != null
                ? employee.getPassword() : dummyHash);

        if (!inLocation || !active || !passwordOk) {
            attempts.recordFailure(scope, username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID);
        }
        attempts.recordSuccess(scope, username);

        return LoginResponseDto.builder()
                .token(jwtService.generate(employee))
                .tokenType("Bearer")
                .employeeId(employee.getId())
                .username(employee.getUsername())
                .role(employee.getRole().name())
                .locationId(locationId)
                .build();
    }
}
