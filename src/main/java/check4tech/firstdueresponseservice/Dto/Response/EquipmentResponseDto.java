package check4tech.firstdueresponseservice.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentResponseDto {
    private Long id;
    private String name;
    private String serialNumber;
    private String warrantyExpiration;
    private int quantity;
    private Integer restockAlert;
}
