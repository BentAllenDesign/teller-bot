package com.bad.studios.tellerbot.controllers;

import com.bad.studios.tellerbot.models.Ticket;
import com.bad.studios.tellerbot.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private TicketService service;

    @Autowired
    public TicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return service.getAllTickets();
    }

    @GetMapping("/mention/{mention}")
    public Ticket getTicketsByUserMention(@PathVariable("mention") String mention) throws Throwable {
        return service.getTicketsByUserMention(mention);
    }

    @GetMapping("/id/{id}")
    public Ticket getTicketsByUserId(@PathVariable("id") String id) throws Throwable {
        return service.getTicketsByUserId(id);
    }


}
