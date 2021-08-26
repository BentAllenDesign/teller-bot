package com.bad.studios.tellerbot.commands;

import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.ApplicationCommandOptionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ReadTicketsByIdCommand implements ApplicationCommandRequest {

    @Override
    public String name() {
        return "read-tickets-by-user";
    }

    @Override
    public String description() {
        return "Read tickets given to a specific user";
    }

    @Override
    public Possible<List<ApplicationCommandOptionData>> options() {
        return Possible.of(Arrays.asList(
                ApplicationCommandOptionData.builder()
                        .name("user")
                        .description("User to get tickets from")
                        .type(ApplicationCommandOptionType.USER.getValue())
                        .required(true)
                        .build()
        ));
    }

    @Override
    public Possible<Boolean> defaultPermission() {
        return Possible.of(true);
    }
}

