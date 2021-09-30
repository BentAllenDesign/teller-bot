package com.bad.studios.tellerbot.commands;

import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.ApplicationCommandOptionType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DeleteRaffleEntryCommand implements ApplicationCommandRequest {

    @Override
    public String name() {
        return "remove-raffle-entries";
    }

    @Override
    public String description() {
        return "Removes n raffle entries";
    }

    @Override
    public Possible<List<ApplicationCommandOptionData>> options() {
        return Possible.of(Collections.singletonList(
                ApplicationCommandOptionData.builder()
                        .name("amount")
                        .description("Number of tickets to remove from raffle. If not supplied, it will remove all tickets")
                        .type(ApplicationCommandOptionType.INTEGER.getValue())
                        .required(true)
                        .build()
        ));
    }

    @Override
    public Possible<Boolean> defaultPermission() {
        return Possible.of(true);
    }
}