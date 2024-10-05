package com.example.hireviewserver.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

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
    private String picture;
    @Setter
    private String job;
    private Integer token;

    public User(String email, String name, String picture) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.job = "프론드엔드 개발자";
        this.token = 10;
    }

    public void decreaseToken() {
        if (this.token > 0) {
            this.token--;
        } else {
            throw new IllegalStateException("Not enough tokens");
        }
    }

}