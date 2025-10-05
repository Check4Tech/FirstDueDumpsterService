package check4tech.firstdueresponseservice.Controller.Authorization;

import check4tech.firstdueresponseservice.Dto.Request.LoginRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.EmployeeResponseDto;
import check4tech.firstdueresponseservice.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthorizationController {

    private final EmployeeService employeeService;

    public AuthorizationController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/location/{locationId}/login")
    public ResponseEntity<String> login(
            @PathVariable Long locationId,
            @RequestBody LoginRequestDto request) {

        // throws if username not found
        EmployeeResponseDto employeeDto = employeeService.getEmployeeByUsernameAndLocation(locationId, request.getUsername());

        // check password
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (passwordEncoder.matches(request.getPassword(), employeeDto.getPassword())) {
            return ResponseEntity.ok("Login successful!");
        } else {
            throw new RuntimeException("Invalid username or password");
        }
    }

}
