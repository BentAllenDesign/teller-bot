package com.bad.studios.tellerbot.commands;

import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.ApplicationCommandOptionType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UpdateNameCommand implements ApplicationCommandRequest {

    @Override
    public String name() {
        return "set-name";
    }

    @Override
    public String description() {
        return "Change your name";
    }

    @Override
    public Possible<List<ApplicationCommandOptionData>> options() {
        return Possible.of(Collections.singletonList(
                ApplicationCommandOptionData.builder()
                        .name("name")
                        .description("Name to change to")
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .required(true)
                        .build()
        ));
    }

    @Override
    public Possible<Boolean> defaultPermission() {
        return Possible.of(true);
    }
}
