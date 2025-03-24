package com.example.hireviewserver.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("users")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private Long id;

    private String email;
    @Setter
    private String name;
    @Setter
    private String picture;
    @Setter
    private String job;
    private Integer token;
    private LocalDate lastAttendanceDate;

    public User(String email, String name, String picture) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.job = "Pending";
        this.token = 0;
        this.lastAttendanceDate = LocalDate.now().minusDays(1);
    }

    public void decreaseToken() {
        if (this.token > 0) {
            this.token--;
        } else {
            throw new IllegalStateException("Not enough tokens");
        }
    }

    public void checkAttendance() {
        LocalDate today = LocalDate.now();

        // 오늘 날짜와 마지막 출석 날짜를 비교
        if (!today.equals(this.lastAttendanceDate)) {
            this.token += 10;
            this.lastAttendanceDate = today;
        }
    }
}