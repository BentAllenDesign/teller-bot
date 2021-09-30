package com.bad.studios.tellerbot.repos;

import com.bad.studios.tellerbot.models.Raffle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RaffleRepo extends JpaRepository<Raffle, Integer> {

    Optional<Raffle> findByTitle (String title);

}
