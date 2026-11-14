package com.nadavramon.job_tracker.repository;

import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.Communication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommunicationRepository extends JpaRepository<Communication, UUID> {

    List<Communication> findByApplicationOrderByOccurredAtDesc(Application application);
}
