package com.bad.studios.tellerbot.config;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.Event;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import lombok.var;
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

    @Bean
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient(List<ReactiveEventAdapter> eventListeners, List<ApplicationCommandRequest> commandRequestList) {

        GatewayDiscordClient client = DiscordClientBuilder.create(token)
                .build()
                .login()
                .block();

        RestClient restClient = client.getRestClient();
        long appId = restClient.getApplicationId().block();

        restClient.getApplicationService().getGlobalApplicationCommands(appId)
                .map(x -> restClient.getApplicationService().deleteGlobalApplicationCommand(appId, Long.parseLong(x.id())))
                .subscribe();

        for(ApplicationCommandRequest request : commandRequestList) {

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

        for(ReactiveEventAdapter listener : eventListeners) {
            client.on(listener).subscribe();
        }

        return client;

        /*if(client != null) {

            ApplicationCommandRequest randomCommand = ApplicationCommandRequest.builder()
                    .name("random")
                    .description("Send a random number")
                    .addOption(ApplicationCommandOptionData.builder()
                            .name("digits")
                            .description("Number of digits")
                            .type(ApplicationCommandOptionType.INTEGER.getValue())
                            .required(false)
                            .build())
                    .build();

            RestClient restClient = client.getRestClient();
            long appId = restClient.getApplicationId().block();
            guildId.set(guild);

            restClient.getApplicationService()
                    .createGuildApplicationCommand(appId, Snowflake.asLong(guildId.get()), randomCommand)
                    .doOnError(e -> log.warn("Unable to create global command", e))
                    .onErrorResume(e -> Mono.empty())
                    .block();

            client.on(ReadyEvent.class)
                    .subscribe(e -> {
                        final User self = e.getSelf();
                        footerPicUri.set(self.getAvatarUrl());
                        System.out.printf(
                                "Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator()
                        );
                    });


            for(EventListener<T> listener : eventListeners) {
                client.on(listener.getEventType())
                        .flatMap(listener::execute)
                        .onErrorResume(listener::handleError)
                        .subscribe();
            }

            client.on(new ReactiveEventAdapter() {
                private final Random random = new Random();
                @Override
                public Publisher<?> onSlashCommand(SlashCommandEvent event) {
                    if (event.getCommandName().equals("random")) {
                        String result = result(random, event.getInteraction().getCommandInteraction().get());
                        return event.reply(result);
                    }
                    return Mono.empty();
                }
            }).blockLast();

            return client;
        }

        return null;*/
    }
}
