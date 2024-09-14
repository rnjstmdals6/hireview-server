package com.example.hireviewserver.common;

import com.example.hireviewserver.common.enums.Job;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CommonService {

    public Mono<JobResponseDTO> getJobs() {
        List<String> jobs =
                Stream.of(Job.values())
                        .map(Job::getDescription)
                        .toList();

        return Mono.just(new JobResponseDTO(jobs));
    }
}
