package net.dzultra.Bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import net.dzultra.Bot.Main;
import net.dzultra.Bot.util.ObjectBuilder;
import net.dzultra.jsa.cards.Card;
import net.dzultra.jsa.cards.CardRequester;
import reactor.core.publisher.Mono;

import java.util.List;

public class GetCardCommand {
    public static ApplicationCommandOptionData cardNameOption = ObjectBuilder.buildOption(
            "name",
            "Name of the card",
            ApplicationCommandOption.Type.STRING,
            true
    );
    public static ApplicationCommandOptionData fuzzySearchOption = ObjectBuilder.buildOption(
            "fuzzy",
            "If the search should be fuzzy",
            ApplicationCommandOption.Type.BOOLEAN,
            true
    );
    public static ApplicationCommandOptionData setCodeOption = ObjectBuilder.buildOption(
            "setcode",
            "SetCode of the card",
            ApplicationCommandOption.Type.STRING,
            false
    );

    
    public static ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name("card")
                .description("Get card based on name and optional SetCode")
                .addOption(cardNameOption)
                .addOption(fuzzySearchOption)
                .addOption(setCodeOption)
                .dmPermission(true)
                .integrationTypes(List.of(0, 1)) // GUILD + USER
                .contexts(List.of(0, 1, 2))      // GUILD + DM + GROUP DM
                .build();
    }

    public static Mono<Void> executeCommand(ChatInputInteractionEvent event) {
        String cardName =  event.getOption(cardNameOption.name())
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElseThrow(() -> new IllegalArgumentException("❌ Missing Option: " + cardNameOption.name()));
        boolean fuzzy =  event.getOption(fuzzySearchOption.name())
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean)
                .orElseThrow(() -> new IllegalArgumentException("❌ Missing Option: " + fuzzySearchOption.name()));
        String setCode = event.getOption(setCodeOption.name())
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString).orElse(null);

        System.out.println("Executing /card command for name: " + cardName + ", setcode: " + setCode);
        return runCmd(event, cardName, fuzzy,setCode);
    }

    public static Mono<Void> runCmd(ChatInputInteractionEvent event, String cardName, boolean fuzzy, String setcode) {
        System.out.println("Processing card: " + cardName);
        try {
            String imageURIFront = null;
            String imageURIBack = null;
            System.out.println("Requesting card from Scryfall: " + cardName + " set: " + setcode);
            Card card = new CardRequester(Main.scryfallClient).getCardByName(cardName, setcode, fuzzy);
            System.out.println("Card retrieved: " + card.name());
            Card.ImageURIs imageURIs = card.image_uris();
            if (imageURIs == null) {
                // Double Sided Card
                System.out.println("Double sided card");
                imageURIs = card.card_faces().getFirst().image_uris();
                imageURIFront = imageURIs.large().toString();
                imageURIs = card.card_faces().get(1).image_uris();
                imageURIBack = imageURIs.large().toString();
            } else {
                // Single Sided Card
                System.out.println("Single sided card");
                imageURIFront = imageURIs.large().toString();
            }

            System.out.println("Creating embed with image: " + imageURIFront);
            EmbedCreateSpec embed;
            if (imageURIBack == null && imageURIFront != null) {
                embed = EmbedCreateSpec.builder()
                        .color(Color.DARK_GRAY)
                        .image(imageURIFront)
                        .build();
                InteractionApplicationCommandCallbackSpec reply = InteractionApplicationCommandCallbackSpec.builder()
                        .addEmbed(embed)
                        .build();
                return event.reply(reply).then();
            } else if (imageURIBack != null && imageURIFront != null) {
                EmbedCreateSpec embedFront = EmbedCreateSpec.builder()
                        .color(Color.DARK_GRAY)
                        .title("Front Face")
                        .image(imageURIFront)
                        .build();
                EmbedCreateSpec embedBack = EmbedCreateSpec.builder()
                        .color(Color.DARK_GRAY)
                        .title("Back Face")
                        .image(imageURIBack)
                        .build();
                InteractionApplicationCommandCallbackSpec reply = InteractionApplicationCommandCallbackSpec.builder()
                        .addEmbed(embedFront)
                        .addEmbed(embedBack)
                        .build();
                return event.reply(reply).then();
            } else {
                return event.reply("❌ No Image available. Refer to " + card.scryfall_uri().toString()).withEphemeral(true).then();
            }
        } catch (Exception e) {
            System.out.println("Exception in runCmd: " + e.getMessage());
            e.printStackTrace();
            return event.reply("❌ Error retrieving card: " + e.getMessage()).withEphemeral(true).then();
        }
    }
}
