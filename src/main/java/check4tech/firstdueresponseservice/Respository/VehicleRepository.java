package check4tech.firstdueresponseservice.Respository;

import check4tech.firstdueresponseservice.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByLocation_IdAndIsCurrentlyOnJobOrderByVehicleYearDesc(Long locationId, Boolean onJob);
    Vehicle findByLocation_IdAndId(Long locationId, Long id);

        @Query("SELECT v FROM Vehicle v " +
                "WHERE v.location.id = :locationId " +
                "AND (:searchTerm IS NULL OR :searchTerm = '' " +
                "OR UPPER(v.vinNumber) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
                "OR UPPER(v.licensePlate) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
        List<Vehicle> searchByLocationAndVinOrLicensePlate(
                @Param("locationId") Long locationId,
                @Param("searchTerm") String searchTerm);
    }
