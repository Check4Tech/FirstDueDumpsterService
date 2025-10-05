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
@Table(name = "TRAILER")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Trailer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotBlank
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "make")
    @NotBlank
    @Size(min = 1, max = 50)
    private String make;

    @Column(name = "model")
    @NotBlank
    @Size(min = 1, max = 50)
    private String model;

    @Column(name = "dmv_inspection")
    private LocalDate dmvInspection;

    @Column(name = "registration")
    private LocalDate registration;

    @Column(name = "is_currently_on_job")
    private Boolean isCurrentlyOnJob = false;

    @Column(name = "trailer_year")
    @NotBlank
    @Size(min = 4, max = 4)
    private String trailerYear;

    @Column(name = "license_plate")
    @Size(min = 1, max = 20)
    private String licensePlate;

    @Column(name = "vin_number")
    @NotBlank
    @Size(min = 1, max = 50)
    private String vinNumber;

    @Column(name = "size")
    @NotNull
    private Integer size;

}
