package com.example.hireviewserver.domain.user.attendance;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeeklyAttendanceDTO {
    private String day;
    private String status;
}