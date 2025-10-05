package check4tech.firstdueresponseservice.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "EQUIPMENT")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "serial_number")
    private String serialNumber = "";

    @Column(name = "warranty_expiration")
    private String warrantyExpiration = null;

    @Column(name = "quantity", nullable = false)
    @NotNull
    private int quantity;

    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "restock_alert", nullable = false)
    @NotNull
    private Integer restockAlert;
}
