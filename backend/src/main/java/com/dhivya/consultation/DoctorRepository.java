package com.dhivya.consultation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface DoctorRepository extends JpaRepository<Doctor,UUID>{
 List<Doctor> findAllByActiveTrue();
 boolean existsByIdAndActiveTrue(UUID id);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select d from Doctor d where d.id=:id and d.active=true") Optional<Doctor> lockActive(@Param("id") UUID id);
}
