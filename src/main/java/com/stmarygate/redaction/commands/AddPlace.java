package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.PlaceEntity;
import com.stmarygate.redaction.entities.RegionEntity;
import com.stmarygate.redaction.entities.VillageEntity;
import java.util.Objects;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class AddPlace extends CommandAbstract {
  public AddPlace(StMaryRedactor client) {
    super(client);

    this.name = "addplace";
    this.description = "Add a place.";

    this.options.add(
        new OptionData(OptionType.STRING, "name", "The name of the place to add.", true));
    this.options.add(
        new OptionData(
            OptionType.STRING, "description", "The description of the place to add.", true));
    this.options.add(
        new OptionData(OptionType.STRING, "region", "The region of the place to add.", true));
    this.options.add(
        new OptionData(OptionType.BOOLEAN, "isinvillage", "If the place is in a village", true));
    this.options.add(
        new OptionData(OptionType.STRING, "village", "The village of the place to add.", false));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    String namePlace = Objects.requireNonNull(event.getOption("name")).getAsString();
    String descriptionPlace = Objects.requireNonNull(event.getOption("description")).getAsString();
    String regionName = Objects.requireNonNull(event.getOption("region")).getAsString();
    boolean isInVillage = Objects.requireNonNull(event.getOption("isinvillage")).getAsBoolean();
    String villageName =
        event.getOption("village") != null
            ? Objects.requireNonNull(event.getOption("village")).getAsString()
            : null;

    PlaceEntity place = new PlaceEntity();
    if (!setPlaceDetails(
        place, namePlace, descriptionPlace, regionName, isInVillage, villageName, event)) {
      return;
    }

    savePlace(event, place);
  }

  private boolean setPlaceDetails(
      PlaceEntity place,
      String namePlace,
      String descriptionPlace,
      String regionName,
      boolean isInVillage,
      String villageName,
      SlashCommandInteractionEvent event) {

    RegionEntity region = DatabaseManager.findByName(regionName, RegionEntity.class);

    if (region == null) {
      sendRegionNotFoundError(event, regionName);
      return false;
    }

    place.setName(namePlace);
    place.setDescription(descriptionPlace);
    place.setRegion(region);

    VillageEntity village = DatabaseManager.findByName(villageName, VillageEntity.class);
    PlaceEntity placeDb = DatabaseManager.findByName(namePlace, PlaceEntity.class);

    if (isInVillage && village == null) {
      sendVillageNotFoundError(event, villageName);
      return false;
    }

    place.setVillage(isInVillage ? village : null);

    if (placeDb != null
        && ((village == null && place.getVillage() == null)
            || (Objects.equals(place.getVillage().getId(), village.getId())))
        && Objects.equals(placeDb.getRegion().getId(), place.getRegion().getId())) {
      sendPlaceAlreadyExistsError(event, place);
      return false;
    }

    return true;
  }

  private void sendPlaceAlreadyExistsError(SlashCommandInteractionEvent event, PlaceEntity place) {
    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Place already exists!", null, event.getUser().getAvatarUrl());
    embed.setDescription("A place with the same name already exists.");
    embed.addField("Name", place.getName(), true);
    embed.addField("Description", place.getDescription(), true);
    embed.addField("Region", place.getRegion().getName(), true);
    embed.setColor(0xff0000);
    embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
    embed.setTimestamp(event.getTimeCreated());

    if (place.getVillage() != null) {
      embed.addField("Village", place.getVillage().getName(), true);
    }

    event.getHook().sendMessageEmbeds(embed.build()).queue();
  }

  private void sendVillageNotFoundError(SlashCommandInteractionEvent event, String villageName) {
    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Village doesn't exist!", null, event.getUser().getAvatarUrl());
    embed.setDescription("A village with the same name doesn't exist.");
    embed.addField("Name", villageName, true);
    embed.setColor(0xff0000);
    embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
    embed.setTimestamp(event.getTimeCreated());

    event.getHook().sendMessageEmbeds(embed.build()).queue();
  }

  private void sendRegionNotFoundError(SlashCommandInteractionEvent event, String regionName) {
    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Region doesn't exist!", null, event.getUser().getAvatarUrl());
    embed.setDescription("A region with the same name doesn't exist.");
    embed.addField("Name", regionName, true);
    embed.setColor(0xff0000);
    embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
    embed.setTimestamp(event.getTimeCreated());

    event.getHook().sendMessageEmbeds(embed.build()).queue();
  }

  private void savePlace(SlashCommandInteractionEvent event, PlaceEntity place) {
    DatabaseManager.save(place);

    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Place added!", null, event.getUser().getAvatarUrl());
    embed.setDescription("The place has been added.");
    embed.addField("Name", place.getName(), true);
    embed.addField("Description", place.getDescription(), true);
    embed.addField("Region", place.getRegion().getName(), true);
    embed.setColor(0x00ff00);
    embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
    embed.setTimestamp(event.getTimeCreated());

    if (place.getVillage() != null) {
      embed.addField("Village", place.getVillage().getName(), true);
    }

    event.getHook().sendMessageEmbeds(embed.build()).queue();
  }
}
