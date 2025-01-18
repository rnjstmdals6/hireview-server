package com.example.hireviewserver.user.attendance;

import com.example.hireviewserver.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserService userService;

    @GetMapping("/weekly")
    public Flux<WeeklyAttendanceDTO> getWeeklyAttendance(
            Mono<Principal> principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedDate) {

        final LocalDate finalRequestedDate = (requestedDate == null) ? LocalDate.now() : requestedDate;

        return principal.map(Principal::getName)
                .flatMap(userService::findUserIdByEmail)
                .flatMapMany(userId -> attendanceService.getWeeklyAttendanceStatus(userId, finalRequestedDate));
    }
}