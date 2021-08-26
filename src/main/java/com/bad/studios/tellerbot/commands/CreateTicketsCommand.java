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
public class CreateTicketsCommand implements ApplicationCommandRequest {

    @Override
    public String name() {
        return "create-tickets";
    }

    @Override
    public String description() {
        return "Award tickets to a user";
    }

    @Override
    public Possible<List<ApplicationCommandOptionData>> options() {
        return Possible.of(Arrays.asList(
                ApplicationCommandOptionData.builder()
                        .name("user")
                        .description("User to award tickets to")
                        .type(ApplicationCommandOptionType.USER.getValue())
                        .required(true)
                        .build(),
                ApplicationCommandOptionData.builder()
                        .name("amount")
                        .description("Number of tickets to award user")
                        .type(ApplicationCommandOptionType.INTEGER.getValue())
                        .required(true)
                        .build(),
                ApplicationCommandOptionData.builder()
                        .name("reason")
                        .description("Reason for awarding tickets")
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .required(false)
                        .build()
        ));
    }

    @Override
    public Possible<Boolean> defaultPermission() {
        return Possible.of(true);
    }
}
