package com.bad.studios.tellerbot.repos;

import com.bad.studios.tellerbot.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepo extends JpaRepository<Ticket, String> {

    Optional<Ticket> findByMentionString(String mentionString);

}
