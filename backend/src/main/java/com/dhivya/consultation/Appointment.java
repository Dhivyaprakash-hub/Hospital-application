package com.dhivya.consultation;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
@Entity @Table(name="appointments",uniqueConstraints=@UniqueConstraint(name="uk_appointment_slot",columnNames={"doctor_id","appointment_date","appointment_time"})) public class Appointment {
 @Id private UUID id;
 @ManyToOne(fetch=FetchType.EAGER,optional=false) @JoinColumn(name="doctor_id",nullable=false) private Doctor doctor;
 @Column(name="appointment_date",nullable=false) private LocalDate appointmentDate;
 @Column(name="appointment_time",nullable=false) private LocalTime appointmentTime;
 @Column(nullable=false,length=20) private String status="CONFIRMED";
 protected Appointment(){}
 public Appointment(UUID id,Doctor doctor,LocalDate date,LocalTime time){this.id=id;this.doctor=doctor;this.appointmentDate=date;this.appointmentTime=time;}
 public UUID getId(){return id;} public Doctor getDoctor(){return doctor;} public LocalDate getAppointmentDate(){return appointmentDate;} public LocalTime getAppointmentTime(){return appointmentTime;} public String getStatus(){return status;}
}
