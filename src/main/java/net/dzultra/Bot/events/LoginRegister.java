package net.dzultra.Bot.events;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import reactor.core.publisher.Mono;

public class LoginRegister {
    public static Mono<Void> getLoginRegister(GatewayDiscordClient gateway) {
        return gateway.on(ReadyEvent.class, event ->
                Mono.fromRunnable(() -> System.out.printf("Logged in as %s%n", event.getSelf().getUsername()))
        ).then();
    }
}
