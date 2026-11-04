package com.dhivya.consultation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
@Service public class AppointmentService {
 private static final List<LocalTime> SLOTS=List.of(LocalTime.of(9,0),LocalTime.of(10,0),LocalTime.of(11,0),LocalTime.of(13,0),LocalTime.of(14,0),LocalTime.of(15,0),LocalTime.of(16,0));
 private static final ZoneId BUSINESS_ZONE=ZoneId.of("Asia/Kolkata");
 private final DoctorRepository doctors; private final AppointmentRepository appointments;
 public AppointmentService(DoctorRepository doctors,AppointmentRepository appointments){this.doctors=doctors;this.appointments=appointments;}
 @Transactional(readOnly=true) public List<Doctor> listDoctors(){return doctors.findAllByActiveTrue();}
 @Transactional(readOnly=true) public List<Slot> available(UUID doctorId,LocalDate date){
  if(!date.isAfter(LocalDate.now(BUSINESS_ZONE))||date.isAfter(LocalDate.now(BUSINESS_ZONE).plusDays(60)))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a date from tomorrow through the next 60 days");
  if(!doctors.existsById(doctorId))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found");
  Set<LocalTime> taken=new HashSet<>();appointments.findAllByDoctorIdAndAppointmentDateAndStatus(doctorId,date,"CONFIRMED").forEach(a->taken.add(a.getAppointmentTime()));
  return SLOTS.stream().map(time->new Slot(time.toString(),!taken.contains(time))).toList();
 }
 @Transactional public Appointment book(UUID doctorId,LocalDate date,LocalTime time){
  LocalDate today=LocalDate.now(BUSINESS_ZONE);
  if(!date.isAfter(today)||date.isAfter(today.plusDays(60)))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a date from tomorrow through the next 60 days");
  if(!SLOTS.contains(time))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a listed appointment time");
  Doctor doctor=doctors.lockActive(doctorId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found"));
  if(appointments.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(doctorId,date,time,"CONFIRMED"))throw new ResponseStatusException(HttpStatus.CONFLICT,"That appointment time has just been reserved");
  return appointments.save(new Appointment(UUID.randomUUID(),doctor,date,time));
 }
 @Transactional(readOnly=true) public Appointment find(UUID reference){return appointments.findById(reference).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Appointment reference not found"));}
 public record Slot(String time,boolean available){}
}
