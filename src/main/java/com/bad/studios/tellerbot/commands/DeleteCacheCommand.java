package com.bad.studios.tellerbot.commands;

import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.possible.Possible;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DeleteCacheCommand implements ApplicationCommandRequest {

    @Override
    public String name() {
        return "clear-cache";
    }

    @Override
    public String description() {
        return "Clears the bot's cache in the case of slow behavior";
    }

    @Override
    public Possible<List<ApplicationCommandOptionData>> options() {
        return Possible.of(Collections.emptyList());
    }

    @Override
    public Possible<Boolean> defaultPermission() {
        return Possible.of(true);
    }
}
