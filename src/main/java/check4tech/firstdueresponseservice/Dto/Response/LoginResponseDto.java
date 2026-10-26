package check4tech.firstdueresponseservice.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private String token;
    private String tokenType;
    private Long employeeId;
    private String username;
    private String role;
    private Long locationId;
}
