package com.example.hireviewserver.user.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public Flux<WeeklyAttendanceDTO> getWeeklyAttendanceStatus(Long userId, LocalDate requestedDate) {
        LocalDate startDate = requestedDate.with(DayOfWeek.MONDAY);
        LocalDate endDate = requestedDate.with(DayOfWeek.SUNDAY);
        List<LocalDate> weekDates = startDate.datesUntil(endDate.plusDays(1)).toList();

        return attendanceRepository.findAttendanceByUserIdAndWeek(userId, startDate, endDate)
                .collectList()
                .flatMapMany(attendedDates -> Flux.fromIterable(weekDates)
                        .map(date -> {
                            String status;
                            if (attendedDates.contains(date)) {
                                status = "attended";
                            } else if (date.isAfter(LocalDate.now())) {
                                status = "future";
                            } else {
                                status = "missed";
                            }
                            String day = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                            return new WeeklyAttendanceDTO(day, status);
                        })
                );
    }

    public Mono<Void> markAttendance(Long userId) {
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByUserIdAndDate(userId, today)
                .switchIfEmpty(attendanceRepository.save(new Attendance(userId, today)))
                .then();
    }
}