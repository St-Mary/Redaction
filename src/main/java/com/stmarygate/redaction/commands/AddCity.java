package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class AddCity extends CommandAbstract {
    public AddCity(StMaryRedactor client) {
        super(client);

        this.name = "addcity";
        this.description = "Add a city to a region.";
        this.options.add(new OptionData(OptionType.STRING, "region", "The region to add the city to.", true));
        this.options.add(new OptionData(OptionType.STRING, "name", "The name of the city to add.", true));
        this.options.add(new OptionData(OptionType.STRING, "description", "The description of the city to add.", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String region = event.getOption("region").getAsString();
        String name = event.getOption("name").getAsString();
        String description = event.getOption("description").getAsString();

        event.reply("Added city " + name + " to region " + region + " with description " + description).queue();
    }
}
