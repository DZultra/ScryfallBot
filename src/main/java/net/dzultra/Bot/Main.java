package net.dzultra.Bot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import net.dzultra.Bot.commands.GetCardCommand;
import net.dzultra.Bot.events.LoginRegister;
import net.dzultra.Bot.util.DataHandler;
import net.dzultra.jsa.ScryfallClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

public class Main {
    public static DiscordClient bot = DiscordClient.create(Objects.requireNonNull(DataHandler.getToken()));
    public static ScryfallClient scryfallClient = new ScryfallClient("ScryfallBot/1.0.0");
    public static long applicationId = Objects.requireNonNull(bot.getApplicationId().block());

    public static void main(String[] args) {

        ApplicationCommandRequest getCardRequest = GetCardCommand.getCommandRequest();

        Mono<Void> login = bot.withGateway((GatewayDiscordClient gateway) -> {
            Mono<Void> loginRegister = LoginRegister.getLoginRegister(gateway);

            Mono<Void> commandRegistration = registerCommands(bot, List.of(getCardRequest));

            Mono<Void> commands = gateway.on(ChatInputInteractionEvent.class, event -> {
                System.out.println("Received interaction: " + event.getCommandName());
                return switch (event.getCommandName()) {
                    case "card" -> {
                        System.out.println("Handling /card command");
                        yield GetCardCommand.executeCommand(event);
                    }
                    default -> {
                        System.out.println("Unknown command: " + event.getCommandName());
                        yield event.reply("❌ Unknown command\nThis command might be deprecated!").withEphemeral(true).then();
                    }
                };
            }).then();
            return commandRegistration
                    .then(loginRegister)
                    .then(commands);
        });
        login.block();
    }

    public static Mono<Void> registerCommands(DiscordClient client, List<ApplicationCommandRequest> commands) {
        System.out.println("Registering commands...");

        return reactor.core.publisher.Flux.fromIterable(commands)
                .flatMap(request -> {
                    System.out.println("Registering command: " + request.name());
                    return client.getApplicationService()
                            .createGlobalApplicationCommand(applicationId, request)
                            .doOnSuccess(cmd -> System.out.println("Successfully registered command: " + cmd.name()))
                            .doOnError(e -> System.out.println("Failed to register command: " + e.getMessage()));
                })
                .then();
    }
}