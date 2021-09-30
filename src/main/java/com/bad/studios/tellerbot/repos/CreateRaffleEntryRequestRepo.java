package com.bad.studios.tellerbot.repos;

import com.bad.studios.tellerbot.models.CreateRaffleEntryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreateRaffleEntryRequestRepo extends JpaRepository<CreateRaffleEntryRequest, String> {
}
