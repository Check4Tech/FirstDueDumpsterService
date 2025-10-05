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
    private String trailerYear;
    private String licensePlate;
    private String vinNumber;
    private Integer size;
}
