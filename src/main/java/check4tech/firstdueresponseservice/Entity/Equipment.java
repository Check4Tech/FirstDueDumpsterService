package check4tech.firstdueresponseservice.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "name")
    private String name;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "warranty_expiration")
    private String warrantyExpiration;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "restock_alert")
    private Integer restockAlert;
}
