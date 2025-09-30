package check4tech.firstdueresponseservice.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrailerResponseDto {
    private Long id;
    private String make;
    private String model;
    private LocalDate dmvInspection;
    private LocalDate registration;
    private Boolean isCurrentlyOnJob;
    private String vehicleYear;
    private String licensePlate;
    private String vinNumber;
    private int width;
    private int length;
    private String size;
}
