package com.bad.studios.tellerbot.repos;

import com.bad.studios.tellerbot.models.RaffleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaffleEntryRepo extends JpaRepository<RaffleEntry, Integer> {

    List<RaffleEntry> findAllByRaffleIdOrderByAmountDesc(Integer raffleId);

}
