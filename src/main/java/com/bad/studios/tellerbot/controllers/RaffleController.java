package com.bad.studios.tellerbot.controllers;

import com.bad.studios.tellerbot.models.Raffle;
import com.bad.studios.tellerbot.service.RaffleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/raffles")
public class RaffleController {

    private RaffleService service;

    @Autowired
    public RaffleController(RaffleService service) {
        this.service = service;
    }

    @GetMapping
    public List<Raffle> getAllRaffles() {
        return service.getAllRafflesEager();
    }

}
