package com.example.hireviewserver.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class JobResponseDTO {
    private List<String> jobs;
}
