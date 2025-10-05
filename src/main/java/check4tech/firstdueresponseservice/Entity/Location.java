package check4tech.firstdueresponseservice.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "LOCATION")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Column(name = "address")
    @NotBlank
    @Size(min = 1, max = 50)
    private String address;

    @Column(name = "state")
    @NotBlank
    @Size(min = 1, max = 50)
    private String state;

    @Column(name = "zip_code")
    @NotBlank
    @Size(min = 1, max = 20)
    private String zipCode;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Vehicle> vehicle;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Employee> employee;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Trailer> trailer;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Equipment> equipment;

    @Column(name = "is_in_business")
    private boolean isInBusiness = false;

}
