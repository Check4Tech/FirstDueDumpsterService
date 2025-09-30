package check4tech.firstdueresponseservice.Respository;

import check4tech.firstdueresponseservice.Entity.Trailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrailerRepository extends JpaRepository<Trailer, Long> {
}
