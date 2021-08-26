package com.bad.studios.tellerbot.service;

import com.bad.studios.tellerbot.models.Ticket;
import com.bad.studios.tellerbot.repos.TicketRepo;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class TicketService {

    private final TicketRepo repo;

    @Autowired
    public TicketService(TicketRepo repo) {
        this.repo = repo;
    }

    public Ticket saveTickets(Ticket ticket) {
        var updateCheck = repo.findById(ticket.getId());
        if(updateCheck.isPresent()) {
            Ticket oldTicket = updateCheck.get();
            oldTicket.setTickets(oldTicket.getTickets() + ticket.getTickets());
            return repo.save(oldTicket);
        }
        return repo.save(ticket);
    }

    /*public Ticket updateTicketDetail(Ticket ticket) {
        return repo.save(ticket);
    }*/

    public List<Ticket> getAllTickets() {
        return repo.findAll();
    }

    public Ticket getTicketsByUserMention(String mention) throws Exception {
        return repo.findByMentionString(mention).orElseThrow((Supplier<Exception>) () -> new EntityNotFoundException("Could not find ticket object by supplied mention string: " + mention));
    }

    public Ticket getTicketsByUserId(String id) throws Exception {
        return repo.findById(id).orElseThrow((Supplier<Exception>) () -> new EntityNotFoundException("Could not find ticket object by supplied user id: " + id));
    }

    public Optional<Ticket> getOptionalTicketsById(String id) {
        return repo.findById(id);
    }

    public Ticket updateTicket(Ticket ticket) throws Exception {
        var updateCheck = repo.findById(ticket.getId()).orElseThrow((Supplier<Exception>) () -> new EntityNotFoundException("Could not find ticket object by supplied user id: " + ticket.getId()));
        return repo.save(ticket);
    }
}
