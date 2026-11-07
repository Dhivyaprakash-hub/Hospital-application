package com.dhivya.consultation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.datasource.url=jdbc:h2:mem:consultation-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class AppointmentConcurrencyTest {
    private static final UUID DOCTOR = UUID.fromString("529dd054-b5ee-48c1-9e61-6d40c1f00101");
    private static final UUID INACTIVE_DOCTOR = UUID.randomUUID();
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");
    private static final LocalTime SLOT = LocalTime.of(9, 0);

    @Autowired AppointmentService service;
    @Autowired AppointmentRepository appointments;
    @Autowired JdbcTemplate database;

    @Test
    void concurrentBookingsCommitExactlyOneAppointment() throws Exception {
        LocalDate date = LocalDate.now(BUSINESS_ZONE).plusDays(2);
        CyclicBarrier start = new CyclicBarrier(2);
        Callable<Integer> reserve = () -> {
            start.await(10, TimeUnit.SECONDS);
            try {
                service.book(DOCTOR, date, SLOT);
                return 201;
            } catch (ResponseStatusException exception) {
                return exception.getStatusCode().value();
            }
        };

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(reserve);
            var second = executor.submit(reserve);
            assertThat(new Integer[]{first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)})
                    .containsExactlyInAnyOrder(201, CONFLICT.value());
            assertThat(appointments.findAllByDoctorIdAndAppointmentDateAndStatus(DOCTOR, date, "CONFIRMED"))
                    .hasSize(1);
        } finally {
            database.update("DELETE FROM appointments WHERE doctor_id = ? AND appointment_date = ?",
                    DOCTOR, date);
        }
    }

    @Test
    void inactiveDoctorsAreAbsentFromAvailabilityAndBooking() {
        database.update("""
                INSERT INTO doctors (id, name, specialty, clinic, location, experience_years, consultation_fee, active)
                VALUES (?, 'Inactive test doctor', 'Cardiology', 'Test clinic', 'Test city', 1, 0, FALSE)
                """, INACTIVE_DOCTOR);
        LocalDate date = LocalDate.now(BUSINESS_ZONE).plusDays(2);
        try {
            assertThatThrownBy(() -> service.available(INACTIVE_DOCTOR, date))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            exception -> assertThat(exception.getStatusCode()).isEqualTo(NOT_FOUND));
            assertThatThrownBy(() -> service.book(INACTIVE_DOCTOR, date, SLOT))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            exception -> assertThat(exception.getStatusCode()).isEqualTo(NOT_FOUND));
        } finally {
            database.update("DELETE FROM doctors WHERE id = ?", INACTIVE_DOCTOR);
        }
    }
}
