package com.dhivya.consultation;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
@Entity @Table(name="doctors") public class Doctor {
 @Id private UUID id;
 @Column(nullable=false,length=100) private String name;
 @Column(nullable=false,length=70) private String specialty;
 @Column(nullable=false,length=120) private String clinic;
 @Column(nullable=false,length=100) private String location;
 @Column(name="experience_years",nullable=false) private int experienceYears;
 @Column(name="consultation_fee",nullable=false,precision=10,scale=2) private BigDecimal consultationFee;
 @Column(nullable=false) private boolean active=true;
 protected Doctor(){}
 public UUID getId(){return id;} public String getName(){return name;} public String getSpecialty(){return specialty;} public String getClinic(){return clinic;} public String getLocation(){return location;} public int getExperienceYears(){return experienceYears;} public BigDecimal getConsultationFee(){return consultationFee;} public boolean isActive(){return active;}
}
