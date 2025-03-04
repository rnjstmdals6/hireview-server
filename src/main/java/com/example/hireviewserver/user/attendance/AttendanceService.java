package com.example.hireviewserver.user.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public Flux<WeeklyAttendanceDTO> getWeeklyAttendanceStatus(Long userId, LocalDate requestedDate) {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDate utcToday = utcNow.toLocalDate();

        LocalDate startDate = requestedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endDate = requestedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        List<LocalDate> weekDates = startDate.datesUntil(endDate.plusDays(1)).toList();

        return attendanceRepository.findAttendanceByUserIdAndWeek(userId, startDate, endDate)
                .collectList()
                .flatMapMany(attendedDates -> Flux.fromIterable(weekDates)
                        .map(date -> {
                            String status;
                            if (date.equals(utcToday)) {
                                status = "today";
                            } else if (attendedDates.contains(date)) {
                                status = "attended";
                            } else if (date.isAfter(utcToday)) {
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
       LocalDate todayUtc = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();

        return attendanceRepository.findByUserIdAndDate(userId, todayUtc)
                .switchIfEmpty(attendanceRepository.save(new Attendance(userId, todayUtc)))
                .then();
    }
}