package com.bad.studios.tellerbot.commands;

import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.ApplicationCommandOptionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class UpdateUserNameCommand implements ApplicationCommandRequest {

    @Override
    public String name() {
        return "set-name-of-user";
    }

    @Override
    public String description() {
        return "Change a specific user's name";
    }

    @Override
    public Possible<List<ApplicationCommandOptionData>> options() {
        return Possible.of(Arrays.asList(
                ApplicationCommandOptionData.builder()
                        .name("user")
                        .description("User whose name to change")
                        .type(ApplicationCommandOptionType.USER.getValue())
                        .required(true)
                        .build(),
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
