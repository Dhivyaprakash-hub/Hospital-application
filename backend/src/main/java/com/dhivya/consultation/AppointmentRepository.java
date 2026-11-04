package com.dhivya.consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.*;
import java.util.*;
public interface AppointmentRepository extends JpaRepository<Appointment,UUID>{
 List<Appointment> findAllByDoctorIdAndAppointmentDateAndStatus(UUID doctorId,LocalDate date,String status);
 boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(UUID doctorId,LocalDate date,LocalTime time,String status);
}
