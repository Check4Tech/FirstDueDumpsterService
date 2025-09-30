package check4tech.firstdueresponseservice.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "VEHICLE")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "make", nullable = false)
    @NotBlank
    @Size(min = 1, max = 50)
    private String make;

    @Column(name = "model", nullable = false)
    @NotBlank
    @Size(min = 1, max = 50)
    private String model;

    @Column(name = "dmv_inspection")
    private LocalDate dmvInspection;

    @Column(name = "registration")
    private LocalDate registration;

    @Column(name = "is_currently_on_job")
    private Boolean isCurrentlyOnJob = false;

    @Column(name = "vehicle_year", nullable = false)
    @NotBlank
    @Size(min = 4, max = 4)
    private String vehicleYear;

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "vin_number")
    @NotBlank
    @Size(min = 1, max = 50)
    private String vinNumber;

    @Column(name = "mileage")
    @NotNull
    private Integer mileage;

}
