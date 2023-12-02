package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.RegionEntity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class AddRegion extends CommandAbstract {

    public AddRegion(StMaryRedactor client) {
        super(client);

        this.name = "addregion";
        this.description = "Add a region to the map.";
        this.options.add(new OptionData(OptionType.STRING, "name", "The name of the region to add.", true));
        this.options.add(new OptionData(OptionType.STRING, "description", "The description of the region to add.", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String name = event.getOption("name").getAsString();
        String description = event.getOption("description").getAsString();
        event.deferReply().queue();

        // Check if the region already exists.
        if (DatabaseManager.findByName(name, RegionEntity.class) != null) {
            event.getHook().sendMessage("Region " + name + " already exists.").queue();
        } else {
            // Create the region.
            RegionEntity region = new RegionEntity();
            region.setName(name);
            region.setDescription(description);
            DatabaseManager.save(region);

            event.getHook().sendMessage("Added region " + name + " with description " + description).queue();
        }
    }
}
