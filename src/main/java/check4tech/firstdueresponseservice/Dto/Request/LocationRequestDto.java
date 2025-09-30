package check4tech.firstdueresponseservice.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationRequestDto {
    private String name;
    private String address;
    private String state;
    private String zipCode;
}
