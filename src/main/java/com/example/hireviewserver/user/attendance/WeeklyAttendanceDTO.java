package com.example.hireviewserver.user.attendance;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeeklyAttendanceDTO {
    private String day;
    private String status;
}