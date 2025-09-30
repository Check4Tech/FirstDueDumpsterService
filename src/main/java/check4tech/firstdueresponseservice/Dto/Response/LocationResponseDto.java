package check4tech.firstdueresponseservice.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationResponseDto {
    private Long id;
    private String name;
    private String address;
    private String state;
    private String zipCode;
}
