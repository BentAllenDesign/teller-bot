package com.bad.studios.tellerbot.repos;

import com.bad.studios.tellerbot.models.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepo extends JpaRepository<UserData, String> {

    Optional<UserData> findByMentionString(String mentionString);
    List<UserData> findAllByOrderByTicketsDesc();

}
