package com.bad.studios.tellerbot.config;

import com.bad.studios.tellerbot.models.UserData;
import com.bad.studios.tellerbot.service.TicketService;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
@PropertySource("classpath:application.yaml")
public class BotConfig {

    private static final Logger log = Loggers.getLogger(BotConfig.class);

    public final static AtomicReference<String> footerPicUri = new AtomicReference<>();

    @Value("${discord.token}")
    private String token;
    @Value("${discord.guildid}")
    private String guildId;
    @Value("${discord.logchannel}")
    private String logChannelId;

    @Autowired
    private TicketService ticketService;

    @Bean
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient(List<ReactiveEventAdapter> slashListeners, List<ApplicationCommandRequest> commandRequestList) {

        GatewayDiscordClient client = DiscordClientBuilder.create(token)
                .build()
                .gateway()
                .setEnabledIntents(IntentSet.all())
                .login()
                .block();

        RestClient restClient = client.getRestClient();
        long appId = restClient.getApplicationId().block();

        // TODO: Factor in a way to check if update/delete flag has been raised on a command and run delete and create in response
        /*System.out.println("Grabbing list of present guilds");
        var guilds = client.getGuilds().collectList().block();
        if(guilds != null)
            guilds.forEach(x -> {
                System.out.println("Scanning guild " + x.getName() + " for commands to remove");
                restClient.getApplicationService().getGuildApplicationCommands(appId, x.getId().asLong())
                        .flatMap(y -> {
                            System.out.println("Deleting command " + y.name() + " in guild " + x.getName());
                            return restClient.getApplicationService()
                                    .deleteGuildApplicationCommand(appId, x.getId().asLong(), Long.parseLong(y.id()));
                        })
                        .blockLast();
            });*/

        client.on(ReadyEvent.class)
                .subscribe(e -> {
                    client.getGuildMembers(Snowflake.of(guildId))
                            .filter(x -> !x.isBot() && ticketService.getUserData(x.getId().asString()) == null)
                            .map(x -> ticketService.createUserData(new UserData(
                                    x,
                                    0
                            )))
                            .blockLast();
                });

        var discordGuildCommands = restClient.getApplicationService()
                .getGuildApplicationCommands(appId, Long.parseLong(guildId))
                .collectList()
                .block();

        commandRequestList.forEach(request -> {
            System.out.println("Loading command data for " + request.name() + "...");
            if (discordGuildCommands != null && discordGuildCommands.stream().noneMatch(x -> x.name().equals(request.name()))) {
                ApplicationCommandRequest command = ApplicationCommandRequest.builder()
                    .name(request.name())
                    .description(request.description())
                    .addAllOptions(request.options().get())
                    .build();
                restClient.getApplicationService()
                    .createGuildApplicationCommand(appId, Snowflake.asLong(guildId), command)
                    .doOnError(e -> log.warn("Unable to create guild command", e))
                    .onErrorResume(e -> Mono.empty())
                    .block();
            }
            System.out.println(request.name() + " command data loaded");
        });

        slashListeners.forEach(x ->
                client.on(x).subscribe()
        );

        return client;
    }
}
