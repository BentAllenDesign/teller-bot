package com.bad.studios.tellerbot.repos;

import com.bad.studios.tellerbot.models.RaffleDeleteRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaffleDeleteRequestRepo extends JpaRepository<RaffleDeleteRequest, Long> {
}
