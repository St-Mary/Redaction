package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.RegionEntity;
import com.stmarygate.redaction.entities.VillageEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class AddVillage extends CommandAbstract {
  public AddVillage(StMaryRedactor client) {
    super(client);

    this.name = "addvillage";
    this.description = "Add a c to a region.";
    this.options.add(
        new OptionData(OptionType.STRING, "region", "The region to add the city to.", true));
    this.options.add(
        new OptionData(OptionType.STRING, "name", "The name of the village to add.", true));
    this.options.add(
        new OptionData(
            OptionType.STRING, "description", "The description of the village to add.", true));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    String region = event.getOption("region").getAsString();
    String name = event.getOption("name").getAsString();
    String description = event.getOption("description").getAsString();

    event.deferReply().queue();

    // Check if the region exists.
    if (DatabaseManager.findByName(region, RegionEntity.class) == null) {
      EmbedBuilder embed = new EmbedBuilder();
      embed.setAuthor("Region does not exist!", null, event.getUser().getAvatarUrl());
      embed.setDescription("The region you specified does not exist.");
      embed.addField("Region", region, true);
      embed.setColor(0xff0000);
      embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
      embed.setTimestamp(event.getTimeCreated());

      event.getHook().sendMessageEmbeds(embed.build()).queue();
    } else {
      // Check if the city already exists.
      if (DatabaseManager.findByName(name, VillageEntity.class) != null) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Village already exists!", null, event.getUser().getAvatarUrl());
        embed.setDescription("A village with the same name already exists.");
        embed.addField("Name", name, true);
        embed.addField("Description", description, true);
        embed.addField("Region", region, true);
        embed.setColor(0xff0000);
        embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
        embed.setTimestamp(event.getTimeCreated());

        event.getHook().sendMessageEmbeds(embed.build()).queue();
      } else {
        // Create the city.
        VillageEntity village = new VillageEntity();
        village.setName(name);
        village.setDescription(description);
        village.setRegion(DatabaseManager.findByName(region, RegionEntity.class));

        DatabaseManager.save(village);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("New village added!", null, event.getUser().getAvatarUrl());
        embed.setDescription("A new village has been added to the map.");
        embed.addField("Name", name, true);
        embed.addField("Description", description, true);
        embed.addField("Region", region, true);
        embed.setColor(0x00ff00);
        embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
        embed.setTimestamp(event.getTimeCreated());

        event.getHook().sendMessageEmbeds(embed.build()).queue();
      }
    }
  }
}
