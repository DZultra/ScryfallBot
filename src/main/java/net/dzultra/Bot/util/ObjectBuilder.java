package net.dzultra.Bot.util;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;

public class ObjectBuilder {
    public static ApplicationCommandOptionData buildOption(String name, String description, ApplicationCommandOption.Type type, boolean required) {
        return ApplicationCommandOptionData.builder()
                .name(name)
                .description(description)
                .type(type.getValue())
                .required(required)
                .build();
    }
}
