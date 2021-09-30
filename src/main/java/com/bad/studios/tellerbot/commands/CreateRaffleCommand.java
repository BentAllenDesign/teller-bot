package com.bad.studios.tellerbot.commands;

import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.ApplicationCommandOptionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CreateRaffleCommand implements ApplicationCommandRequest {

    @Override
    public String name() {
        return "create-raffle";
    }

    @Override
    public String description() {
        return "Creates a raffle";
    }

    @Override
    public Possible<List<ApplicationCommandOptionData>> options() {
        return Possible.of(Arrays.asList(
                ApplicationCommandOptionData.builder()
                        .name("title")
                        .description("Raffle title")
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .required(true)
                        .build(),
                ApplicationCommandOptionData.builder()
                        .name("description")
                    .description("Raffle description")
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .required(true)
                        .build(),
                ApplicationCommandOptionData.builder()
                        .name("time")
                        .description("Length of the raffle in minutes")
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
