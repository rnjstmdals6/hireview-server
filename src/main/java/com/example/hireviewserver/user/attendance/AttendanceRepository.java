package com.example.hireviewserver.user.attendance;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface AttendanceRepository extends ReactiveCrudRepository<Attendance, Long> {

    @Query("""
        SELECT attendance_date
        FROM attendance
        WHERE user_id = :userId
          AND attendance_date BETWEEN :startDate AND :endDate
    """)
    Flux<LocalDate> findAttendanceByUserIdAndWeek(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT * FROM attendance WHERE user_id = :userId AND attendance_date = :date")
    Mono<Attendance> findByUserIdAndDate(Long userId, LocalDate date);
}