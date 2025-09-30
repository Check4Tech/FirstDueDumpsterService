package check4tech.firstdueresponseservice.Respository;

import check4tech.firstdueresponseservice.Entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location,Long> {
}
