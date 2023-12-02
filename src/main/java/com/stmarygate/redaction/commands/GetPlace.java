package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.PlaceEntity;
import java.util.Objects;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class GetPlace extends CommandAbstract {
  public GetPlace(StMaryRedactor client) {
    super(client);
    this.name = "getplace";
    this.description = "Get a place.";
    this.options.add(new OptionData(OptionType.STRING, "place", "The place to get.", true));
    this.options.add(
        new OptionData(OptionType.STRING, "region", "The region of the place to get.", true));
    this.options.add(
        new OptionData(OptionType.BOOLEAN, "isinvillage", "If the place is in a village", true));
    this.options.add(
        new OptionData(OptionType.STRING, "village", "The village of the place to get.", false));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    String name = Objects.requireNonNull(event.getOption("place")).getAsString();
    String regionName = Objects.requireNonNull(event.getOption("region")).getAsString();
    boolean isInVillage = Objects.requireNonNull(event.getOption("isinvillage")).getAsBoolean();
    String villageName =
        event.getOption("village") != null
            ? Objects.requireNonNull(event.getOption("village")).getAsString()
            : null;

    PlaceEntity place = DatabaseManager.findByName(name, PlaceEntity.class);
    String placeVillageName =
        (place != null && place.getVillage() != null) ? place.getVillage().getName() : null;

    if (place == null
        || (!place.getRegion().getNameWithoutEmote().equals(regionName)
            && (Objects.equals(placeVillageName, villageName) && isInVillage))) {
      event.getHook().sendMessage("This place doesn't exist.").queue();
      return;
    }

    String formattedText =
        "╭───────────┈ ➤ ✎ **"
            + "\uD83C\uDF32 Place Information"
            + "**\n- "
            + "**Name:** `"
            + place.getName()
            + "`\n- "
            + "\uD83C\uDF67 **Description:** "
            + place.getDescription()
            + "\n"
            + "\uD83D\uDD16 **Village:** "
            + formatVillage(place)
            + "\n"
            + "\uD83D\uDC88 **Places:** "
            + formatPlace(place)
            + "\n╰─────────── ·\uFEFF \uFEFF \uFEFF· \uFEFF ·\uFEFF \uFEFF \uFEFF· \uFEFF✦";

    event.getHook().sendMessage(formattedText).queue();
  }

  private String formatVillage(PlaceEntity place) {
    return (place.getVillage() != null) ? place.getVillage().getName() : "None";
  }

  private String formatPlace(PlaceEntity place) {
    return place.getRegion().getName();
  }
}
