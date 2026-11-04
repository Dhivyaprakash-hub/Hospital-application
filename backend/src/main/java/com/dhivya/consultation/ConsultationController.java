package com.dhivya.consultation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;
@RestController @RequestMapping("/api/v1") public class ConsultationController {
 private final AppointmentService service; public ConsultationController(AppointmentService service){this.service=service;}
 @GetMapping("/doctors") public List<DoctorView> doctors(@RequestParam(required=false) String query,@RequestParam(required=false) String specialty){String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);String s=specialty==null?"":specialty.trim().toLowerCase(Locale.ROOT);return service.listDoctors().stream().filter(d->q.isEmpty()||(d.getName()+" "+d.getSpecialty()+" "+d.getClinic()+" "+d.getLocation()).toLowerCase(Locale.ROOT).contains(q)).filter(d->s.isEmpty()||d.getSpecialty().equalsIgnoreCase(specialty.trim())).map(DoctorView::from).toList();}
 @GetMapping("/doctors/{id}/slots") public List<AppointmentService.Slot> slots(@PathVariable UUID id,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date){return service.available(id,date);}
 @PostMapping("/appointments") @ResponseStatus(HttpStatus.CREATED) public AppointmentView book(@Valid @RequestBody BookRequest request){return AppointmentView.from(service.book(request.doctorId(),request.date(),request.time()));}
 @GetMapping("/appointments/{reference}") public AppointmentView appointment(@PathVariable UUID reference){return AppointmentView.from(service.find(reference));}
 public record BookRequest(@NotNull UUID doctorId,@NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date,@NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.TIME) LocalTime time){}
 public record DoctorView(UUID id,String name,String specialty,String clinic,String location,int experienceYears,java.math.BigDecimal consultationFee){static DoctorView from(Doctor d){return new DoctorView(d.getId(),d.getName(),d.getSpecialty(),d.getClinic(),d.getLocation(),d.getExperienceYears(),d.getConsultationFee());}}
 public record AppointmentView(UUID reference,UUID doctorId,String doctor,String specialty,LocalDate date,LocalTime time,String status){static AppointmentView from(Appointment a){return new AppointmentView(a.getId(),a.getDoctor().getId(),a.getDoctor().getName(),a.getDoctor().getSpecialty(),a.getAppointmentDate(),a.getAppointmentTime(),a.getStatus());}}
}
