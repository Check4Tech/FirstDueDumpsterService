package check4tech.firstdueresponseservice.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentRequestDto {

    @NotNull(message = "Location ID is null")
    private Long locationId;

    @NotBlank(message = "Equipment name is required")
    private String name;

    private String serialNumber;
    private String warrantyExpiration;

    @NotNull(message = "Quantity is required")
    private int quantity;

    private BigDecimal cost;

    @NotBlank(message = "Restock Alert is required")
    private Integer restockAlert;
}
