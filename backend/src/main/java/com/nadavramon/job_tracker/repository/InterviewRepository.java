package com.nadavramon.job_tracker.repository;

import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.Interview;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.enums.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    List<Interview> findByApplicationOrderByScheduledAtAsc(Application application);

    List<Interview> findByUserAndResultAndScheduledAtBetweenOrderByScheduledAtAsc(User user, InterviewResult result, LocalDateTime start, LocalDateTime end);
}
