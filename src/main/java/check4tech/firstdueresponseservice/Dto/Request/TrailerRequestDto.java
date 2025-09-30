package check4tech.firstdueresponseservice.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrailerRequestDto {

    @NotNull(message = "Location ID is null")
    private Long locationId;

    @NotBlank(message = "Trailer make is required")
    private String make;

    @NotBlank(message = "Trailer model is required")
    private String model;

    private LocalDate dmvInspection;
    private LocalDate registration;

    @NotBlank(message = "Trailer isCurrentlyOnJob is required")
    private Boolean isCurrentlyOnJob;

    @NotBlank(message = "Trailer year is required")
    private String vehicleYear;

    @NotBlank(message = "Trailer license plate is required")
    @Size(min = 1, max = 20)
    private String licensePlate;

    @NotBlank(message = "Trailer Vin Number is required")
    private String vinNumber;

    @NotNull(message = "Trailer width is required")
    private int width;

    @NotBlank(message = "Trailer length is required")
    private int length;

    private String size = width + " x " + length;
}
