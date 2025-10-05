package check4tech.firstdueresponseservice.Respository;

import aj.org.objectweb.asm.commons.Remapper;
import check4tech.firstdueresponseservice.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByLocation_IdAndIsTerminated(Long locationId, Boolean isTerminated);
    List<Employee> findAllByLicenseExpirationBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT e FROM Employee e " +
            "WHERE e.location.id = :locationId " +
            "AND (:searchTerm IS NULL OR :searchTerm = '' " +
            "OR UPPER(e.firstName) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
            "OR UPPER(e.lastName) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    List<Employee> searchEmployeeByLocationAndSearchTerm(
            @Param("locationId") Long locationId,
            @Param("searchTerm") String searchTerm);

    Optional<Employee> findByLocation_IdAndId(Long locationId, Long id);

    Optional<Employee> findByLocation_IdAndUsernameContainingIgnoreCase(Long locationId, String username);

    Optional<Employee> findByEmail(String Email);

    Optional<Employee> findByUsername(String username);
}
