package check4tech.firstdueresponseservice.Dto.Request;

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
public class VehicleRequestDto {

    @NotNull(message = "Location ID is null")
    private Long locationId;

    @NotBlank(message = "Vehicle make is required")
    private String make;

    @NotBlank(message = "Vehicle model is required")
    private String model;

    private LocalDate dmvInspection;

    private LocalDate registration;

    @NotBlank(message = "Vehicle isCurrentlyOnJob is required")
    private Boolean isCurrentlyOnJob;

    @NotBlank(message = "Vehicle year is required")
    private String vehicleYear;

    @NotBlank(message = "Vehicle license plate is required")
    private String licensePlate;

    @NotBlank(message = "Vehicle Vin Number is required")
    private String vinNumber;

    private Integer mileage;
}
