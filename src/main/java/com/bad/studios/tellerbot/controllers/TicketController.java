package com.bad.studios.tellerbot.controllers;

import com.bad.studios.tellerbot.models.UserData;
import com.bad.studios.tellerbot.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private TicketService service;

    @Autowired
    public TicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserData> getAllTickets() {
        return service.getAllUserData();
    }

    @GetMapping("/id/{id}")
    public UserData getUserData(@PathVariable("id") String id) throws Throwable {
        return service.getUserData(id);
    }


}
