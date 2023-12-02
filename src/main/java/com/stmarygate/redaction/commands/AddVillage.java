package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.RegionEntity;
import com.stmarygate.redaction.entities.VillageEntity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class AddVillage extends CommandAbstract {
    public AddVillage(StMaryRedactor client) {
        super(client);

        this.name = "addvillage";
        this.description = "Add a c to a region.";
        this.options.add(new OptionData(OptionType.STRING, "region", "The region to add the city to.", true));
        this.options.add(new OptionData(OptionType.STRING, "name", "The name of the village to add.", true));
        this.options.add(new OptionData(OptionType.STRING, "description", "The description of the village to add.", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String region = event.getOption("region").getAsString();
        String name = event.getOption("name").getAsString();
        String description = event.getOption("description").getAsString();

        event.deferReply().queue();

        // Check if the region exists.
        if (DatabaseManager.findByName(region, RegionEntity.class) == null) {
            event.getHook().sendMessage("Region " + region + " does not exist.").queue();
        } else {
            // Check if the city already exists.
            if (DatabaseManager.findByName(name, VillageEntity.class) != null) {
                event.getHook().sendMessage("Village " + name + " already exists.").queue();
            } else {
                // Create the city.
                VillageEntity village = new VillageEntity();
                village.setName(name);
                village.setDescription(description);
                village.setRegion(DatabaseManager.findByName(region, RegionEntity.class));

                DatabaseManager.save(village);

                event.getHook().sendMessage("Added village " + name + " with description " + description + " to region " + region).queue();
            }
        }
    }
}
