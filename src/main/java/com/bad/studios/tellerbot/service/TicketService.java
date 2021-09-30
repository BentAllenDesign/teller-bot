package com.bad.studios.tellerbot.service;

import com.bad.studios.tellerbot.models.UserData;
import com.bad.studios.tellerbot.repos.TicketRepo;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepo repo;

    @Autowired
    public TicketService(TicketRepo repo) {
        this.repo = repo;
    }

    @Caching(
            put = @CachePut(value = "user", key = "#result.id"),
            evict = @CacheEvict(value = "users", allEntries = true)
    )
    public UserData createUserData(UserData userData) {
        return repo.save(userData);
    }

    @Caching(
            put = @CachePut(value = "user", key = "#id"),
            evict = @CacheEvict(value = "users", allEntries = true)
    )
    public UserData addTicketsToUser(String id, Integer amount) {
        var user = repo.findById(id).orElseThrow(() -> new RuntimeException("User with id " + id + " could not be found in the database"));
        if(user == null)
            return null;

        return repo.save(user.setTickets(user.getTickets() + Math.abs(amount)));
    }

    @Caching(
            put = @CachePut(value = "user", key = "#id"),
            evict = @CacheEvict(value = "users", allEntries = true)
    )
    public UserData removeTicketsFromUser(String id, Integer amount) {
        var user = repo.findById(id).orElseThrow(() -> new RuntimeException("User with id " + id + " could not be found in the database"));
        if(user == null)
            return null;

        return repo.save(user.setTickets(Math.max(0, user.getTickets() - Math.abs(amount))));
    }

    @Cacheable(cacheNames = "users", unless = "#result==null")
    public List<UserData> getAllUserData() {
        return repo.findAllByOrderByTicketsDesc();
    }

    @Cacheable(cacheNames = "user", key = "#id", unless = "#result==null")
    public UserData getUserData(String id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("User with id " + id + " could not be found in the database"));
    }

    @Caching(
            put = @CachePut(value = "user", key = "#result.id"),
            evict = @CacheEvict(value = "users", allEntries = true)
    )
    public UserData updateUser(UserData userData) {
        var updateCheck = repo.findById(userData.getId());
        if(updateCheck.isPresent())
            return repo.save(userData);
        return null;
    }
}
