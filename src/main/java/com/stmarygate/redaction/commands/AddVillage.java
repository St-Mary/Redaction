package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.RedactorTextManager;
import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.RegionEntity;
import com.stmarygate.redaction.entities.VillageEntity;
import java.util.Objects;
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
        new OptionData(OptionType.STRING, "emote", "The emote of the village to add.", true));
    this.options.add(
        new OptionData(OptionType.STRING, "name", "The name of the village to add.", true));
    this.options.add(
        new OptionData(
            OptionType.STRING, "description", "The description of the village to add.", true));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    String region = Objects.requireNonNull(event.getOption("region")).getAsString();
    String emote = Objects.requireNonNull(event.getOption("emote")).getAsString();
    String name = event.getOption("name").getAsString();
    String description = event.getOption("description").getAsString();

    if (regionDoesNotExist(event, region)) {
      return;
    } else if (villageExists(event, name)) {
      return;
    }

    createAndSaveVillage(event, region, emote, name, description);
  }

  private boolean regionDoesNotExist(SlashCommandInteractionEvent event, String region) {
    if (DatabaseManager.findByName(region, RegionEntity.class) == null) {
      RedactorTextManager.sendRegionNotFoundError(event, region);
      return true;
    }
    return false;
  }

  private boolean villageExists(SlashCommandInteractionEvent event, String name) {
    if (DatabaseManager.findByName(name, VillageEntity.class) != null) {
      RedactorTextManager.sendErrorMessage(
          event,
          "Village already exists in this region.",
          "Please specify a different village name.");
      return true;
    }
    return false;
  }

  private void createAndSaveVillage(
      SlashCommandInteractionEvent event,
      String region,
      String emote,
      String name,
      String description) {
    VillageEntity village = new VillageEntity();
    village.setName(name);
    village.setEmote(emote);
    village.setDescription(description);
    village.setRegion(DatabaseManager.findByName(region, RegionEntity.class));

    DatabaseManager.save(village);

    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("New village added!", null, event.getUser().getAvatarUrl());
    embed.setDescription("A new village has been added to the map.");
    embed.addField("Name", name, true);
    embed.addField("Region", region, true);
    embed.setColor(0x00ff00);
    embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
    embed.setTimestamp(event.getTimeCreated());

    event.getHook().sendMessageEmbeds(embed.build()).queue();
  }
}
