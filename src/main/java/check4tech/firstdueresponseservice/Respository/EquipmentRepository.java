package check4tech.firstdueresponseservice.Respository;

import check4tech.firstdueresponseservice.Entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByLocation_IdOrderByNameAsc(Long locationId);
    Equipment findByIdAndLocation_Id(Long id, Long locationId);

    @Query("SELECT e FROM Equipment e " +
            "WHERE e.location.id = :locationId " +
            "AND (:searchTerm IS NULL OR :searchTerm = '' " +
            "OR UPPER(e.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
            "OR UPPER(e.serialNumber) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    List<Equipment> searchByLocationAndNameOrSerial(
            @Param("locationId") Long locationId,
            @Param("searchTerm") String searchTerm);


}
