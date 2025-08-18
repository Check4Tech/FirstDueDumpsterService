package check4tech.firstdueresponseservice.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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

    @Column(name = "make")
    private String make;

    @Column(name = "model")
    private String model;

    @Column(name = "dmv_inspection")
    private LocalDate dmvInspection;

    @Column(name = "registration")
    private LocalDate registration;

    @Column(name = "is_currently_on_job")
    private Boolean isCurrentlyOnJob;

    @Column(name = "vehicle_year")
    private String vehicleYear;

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "vin_number")
    private String vinNumber;

    @Column(name = "mileage")
    private Integer mileage;

}
