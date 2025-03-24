package com.example.hireviewserver.domain.user.attendance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("attendance")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {
    @Id
    private Long id;
    private Long userId;
    private LocalDate attendanceDate;
    private LocalDateTime createdAt;

    public Attendance(Long userId, LocalDate attendanceDate) {
        this.userId = userId;
        this.attendanceDate = attendanceDate;
        this.createdAt = LocalDateTime.now();
    }
}