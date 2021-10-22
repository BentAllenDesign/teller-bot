package com.bad.studios.tellerbot.service;

import com.bad.studios.tellerbot.events.create.CreateRaffleSlashEvent;
import com.bad.studios.tellerbot.events.delete.DeleteRaffleSlashEvent;
import com.bad.studios.tellerbot.models.Raffle;
import com.bad.studios.tellerbot.models.RaffleEntry;
import com.bad.studios.tellerbot.repos.RaffleDeleteRequestRepo;
import com.bad.studios.tellerbot.repos.RaffleEntryRepo;
import com.bad.studios.tellerbot.repos.RaffleRepo;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 ********************
 ** RAFFLE SERVICE **
 ********************
 *
 * This class acts as the business layer for operations on repos:
 * @see RaffleRepo                  stores raffle information
 * @see RaffleEntryRepo             stores raffle entry information
 * @see RaffleDeleteRequestRepo     TODO stores raffle deletion request for confirmation
 *
 * Most of your logic should live here so that your access layers
 * ({@link CreateRaffleSlashEvent}, {@link DeleteRaffleSlashEvent}, etc...)
 * only need to worry about passing the right data
 *
 * This class is instatiated through the Spring bean collector
 * using the {@link Service} component identifier.
 *
 * To use the Spring-managed RaffleService object, you can put the
 * {@link Autowired} annotation on any field of this type, or above a
 * class' public constructor given that this class is passed in as
 * a parameter. No new object instantiation is needed
 */
@Service
public class RaffleService {

    private final RaffleRepo raffleRepo; // Raffle crud operations
    private final RaffleEntryRepo raffleEntryRepo; // Raffle entry crud operations
    private final RaffleDeleteRequestRepo raffleDeleteRequestRepo; // TODO Raffle delete request crud operations

    @Autowired
    public RaffleService (RaffleRepo raffleRepo, RaffleEntryRepo raffleEntryRepo, RaffleDeleteRequestRepo raffleDeleteRequestRepo) {
        this.raffleRepo = raffleRepo;
        this.raffleEntryRepo = raffleEntryRepo;
        this.raffleDeleteRequestRepo = raffleDeleteRequestRepo;
    }

    /*
     **************************
     *****  Raffle Logic  *****
     **************************
     */

    @Caching(
            put = {
                    @CachePut(value = "raffle_soft", key = "#result.id", unless = "#result==null"),
                    @CachePut(value = "raffle_title", key = "#result.title", unless = "#result==null")
            },
            evict = {
                    @CacheEvict(value = "raffles_soft", allEntries = true),
                    @CacheEvict(value = "raffles_eager", allEntries = true),
                    @CacheEvict(value = "raffle_eager", allEntries = true),
                    @CacheEvict(value = "raffle_title", allEntries = true)
            })
    public Raffle createRaffle(Raffle raffle) {
        return raffleRepo.save(raffle);
    }

    @Cacheable(cacheNames = "raffles_soft", unless = "#result==null")
    public List<Raffle> getAllRafflesSoft() {
        return raffleRepo.findAll();
    }

    @Cacheable(cacheNames = "raffles_eager", unless = "#result==null")
    public List<Raffle> getAllRafflesEager() {
        var raffles = raffleRepo.findAll();
        return raffles.stream()
                .map(this::buildRaffleView)
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "raffle_soft", key = "#id", unless = "#result==null")
    public Raffle getRaffleByIdSoft(Integer id) {
        return raffleRepo.findById(id).orElseThrow(() -> new RuntimeException("Raffle with id " + id + " could not be found in the database"));
    }

    @Cacheable(cacheNames = "raffle_eager", key = "#id", unless = "#result==null")
    public Raffle getRaffleByIdEager(Integer id) {
        var raffle = raffleRepo.findById(id);
        return raffle.map(this::buildRaffleView).orElseThrow(() -> new RuntimeException("Raffle with id " + id + " could not be found in the database"));
    }

    @Cacheable(cacheNames = "raffle_title", key = "#title", unless = "#result==null")
    public Raffle getRaffleByTitle(String title) {
        return raffleRepo.findByTitle(title).orElseThrow(() -> new RuntimeException("Raffle with title " + title + " could not be found in the database"));
    }

    @Caching(evict = {
            @CacheEvict(value = "raffles_soft", allEntries = true),
            @CacheEvict(value = "raffles_eager", allEntries = true),
            @CacheEvict(value = "raffle_soft", allEntries = true),
            @CacheEvict(value = "raffle_eager", allEntries = true),
            @CacheEvict(value = "raffle_title", allEntries = true),
            @CacheEvict(value = "raffle_entries", allEntries = true),
            @CacheEvict(value = "raffle_entry", key = "#raffleId", allEntries = true)
    })
    public void deleteRaffle(Integer raffleId) {
        raffleRepo.deleteById(raffleId);
    }


    /*
     **************************
     **  Raffle Entry Logic  **
     **************************
     */

    @Caching(
            put = {
                    @CachePut(value = "raffle_entry", key = "#result.id", unless = "#result==null")
            },
            evict = {
                    @CacheEvict(value = "raffles_eager", allEntries = true),
                    @CacheEvict(value = "raffle_eager", allEntries = true),
                    @CacheEvict(value = "raffle_entries", allEntries = true)
            })
    public RaffleEntry createRaffleEntry(RaffleEntry entry) {
        var raffle = raffleRepo.findById(entry.getRaffleId());
        if(!raffle.isPresent())
            return null;

        return raffleEntryRepo.save(entry);
    }

    @Cacheable(cacheNames = "raffle_entries", key = "#id", unless = "#result==null")
    public List<RaffleEntry> getRaffleEntriesByRaffleId(Integer id) {
        return raffleEntryRepo.findAllByRaffleIdOrderByAmountDesc(id);
    }

    @Caching(
            put = @CachePut(value = "raffle_entry", key = "#entry.id", unless = "#result==null"),
            evict = {
                    @CacheEvict(value = "raffles_eager", allEntries = true),
                    @CacheEvict(value = "raffle_eager", allEntries = true),
                    @CacheEvict(value = "raffle_entries", allEntries = true)
            }
    )
    public RaffleEntry updateRaffleEntry(RaffleEntry entry) {
        return raffleEntryRepo.save(entry);
    }

    @Caching(evict = {
            @CacheEvict(value = "raffles_eager", allEntries = true),
            @CacheEvict(value = "raffle_eager", allEntries = true),
            @CacheEvict(value = "raffle_entries", allEntries = true),
            @CacheEvict(value = "raffle_entry", key = "#entry.id")
    })
    public void deleteRaffleEntry(RaffleEntry entry) {
        raffleEntryRepo.delete(entry);
    }


    /**
     * TODO
     *
     * The following few methods are set up for the raffle deletion
     *
     *
     * public RaffleDeleteRequest createRaffleDeleteRequest(RaffleDeleteRequest request) {
     *     return raffleDeleteRequestRepo.save(request);
     * }
     * public RaffleDeleteRequest getRaffleDeleteRequestById(Long id) {
     *     return raffleDeleteRequestRepo.findById(id).orElse(null);
     * }
     * public void deleteRaffleDeleteRequestById(Long id) {
     *     raffleDeleteRequestRepo.deleteById(id);
     * }
     */


    public String declareRaffleWinner(Integer id) {
        var raffle = getRaffleByIdEager(id);
        if(raffle == null) {
            return null;
        }
        if(raffle.getTickets().isEmpty()) {
            deleteRaffle(id);
            return null;
        }

        List<String> entries = new ArrayList<>();
        raffle.getTickets().forEach(x -> {
            for(int i = 0; i < x.getAmount(); i++)
                entries.add(x.getUserId());
        });
        Collections.shuffle(entries);

        deleteRaffle(id);

        return entries.get(new Random().nextInt(entries.size()));
    }

    private Raffle buildRaffleView(Raffle raffle) {
        raffle.setTickets(getRaffleEntriesByRaffleId(raffle.getId()));
        return raffle;
    }

}
