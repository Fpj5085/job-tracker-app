package com.nadavramon.job_tracker.repository;

import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.Offer;
import com.nadavramon.job_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

    List<Offer> findByApplication(Application application);

    List<Offer> findByUser(User user);
}
