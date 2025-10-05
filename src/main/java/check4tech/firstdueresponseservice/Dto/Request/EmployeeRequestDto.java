package check4tech.firstdueresponseservice.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDto {

    @NotNull(message = "Location ID is null")
    private Long locationId;

    @NotBlank(message = "First Name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    private String username;
    private String password;
    private String role;

    private String ssn;
    private String licenseId;
    private String licenseType;
    private LocalDate licenseExpiration;

    private Boolean isTerminated;
}
