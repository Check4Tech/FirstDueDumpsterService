package check4tech.firstdueresponseservice.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "EMPLOYEE")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "first_name")
    @NotBlank
    @Size(min = 1, max = 50)
    private String firstName;

    @Column(name = "middle_name")
    @Size(min = 1, max = 50)
    private String middleName;

    @Column(name = "last_name")
    @NotBlank
    @Size(min = 1, max = 50)
    private String lastName;

    @Email
    @NotBlank
    @Size(min = 1, max = 100)
    private String email;

    @Column(name = "ssn")
    private String ssn;

    @Column(name = "license_id")
    private String licenseId;

    @Column(name = "license_type")
    private String licenseType;

    @Column(name = "license_expiration")
    private LocalDate licenseExpiration;
}
