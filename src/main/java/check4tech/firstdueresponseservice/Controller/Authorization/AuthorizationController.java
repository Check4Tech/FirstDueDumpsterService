package check4tech.firstdueresponseservice.Controller.Authorization;

import check4tech.firstdueresponseservice.Dto.Request.LoginRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.LoginResponseDto;
import check4tech.firstdueresponseservice.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthorizationController {

    private final AuthService authService;

    public AuthorizationController(AuthService authService) {
        this.authService = authService;
    }

    /** Returns a bearer token. Send it as "Authorization: Bearer ..." on every other request. */
    @PostMapping("/location/{locationId}/login")
    public ResponseEntity<LoginResponseDto> login(
            @PathVariable Long locationId,
            @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(locationId, request));
    }
}
