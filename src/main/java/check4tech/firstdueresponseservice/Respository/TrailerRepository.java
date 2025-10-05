package check4tech.firstdueresponseservice.Respository;

import check4tech.firstdueresponseservice.Entity.Trailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrailerRepository extends JpaRepository<Trailer, Long> {
    List<Trailer> findAllByLocation_IdOrderBySizeAsc(Long locationId);

    Optional<Trailer> findByIdAndLocation_Id(Long trailerId, Long locationId);

    List<Trailer> findAllByOrderBySizeAsc();

    @Query("SELECT t FROM Trailer t " +
            "WHERE t.location.id = :locationId " +
            "AND (:searchTerm IS NULL OR :searchTerm = '' " +
            "OR UPPER(t.vinNumber) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
            "OR UPPER(t.licensePlate) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
            "OR UPPER(t.make) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    List<Trailer> searchByLocationAndSearchTerm(
            @Param("locationId") Long locationId,
            @Param("searchTerm") String searchTerm);


    List<Trailer> findBySizeBetween(int min, int max); // <-- for ranges
}
