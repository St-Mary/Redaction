package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.RedactorTextManager;
import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.PlaceEntity;
import com.stmarygate.redaction.entities.RegionEntity;
import com.stmarygate.redaction.entities.VillageEntity;
import java.util.Objects;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class EditPlace extends CommandAbstract {
  public EditPlace(StMaryRedactor client) {
    super(client);
    this.name = "editplace";
    this.description = "Edit a place.";
    this.options.add(
        new OptionData(OptionType.STRING, "name", "The name of the place to edit.", true));
    this.options.add(
        new OptionData(OptionType.STRING, "region", "The region of the place to edit.", true));
    this.options.add(
        new OptionData(OptionType.BOOLEAN, "isinvillage", "If the place is in a village", true));
    this.options.add(
        new OptionData(
            OptionType.BOOLEAN, "newisinvillage", "If the place is now in a village", true));
    this.options.add(
        new OptionData(OptionType.STRING, "village", "The village of the place to edit.", false));
    this.options.add(
        new OptionData(OptionType.STRING, "newname", "The new name of the place to edit.", false));
    this.options.add(
        new OptionData(
            OptionType.STRING, "newdescription", "The description of the place to edit.", false));
    this.options.add(
        new OptionData(
            OptionType.STRING, "newvillage", "The new village of the place to edit.", false));
    this.options.add(
        new OptionData(
            OptionType.STRING, "newregion", "The new region of the place to edit.", false));
    this.options.add(
        new OptionData(
            OptionType.STRING, "newemote", "The new emote of the place to edit.", false));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    String name = Objects.requireNonNull(event.getOption("name")).getAsString();
    String regionName = Objects.requireNonNull(event.getOption("region")).getAsString();
    boolean isInVillage = Objects.requireNonNull(event.getOption("isinvillage")).getAsBoolean();
    boolean newIsInVillage = event.getOption("newisinvillage").getAsBoolean();
    OptionMapping villageName = event.getOption("village");
    OptionMapping newName = event.getOption("newname");
    OptionMapping newDescription = event.getOption("newdescription");
    OptionMapping newVillageName = event.getOption("newvillage");
    OptionMapping newRegionName = event.getOption("newregion");
    OptionMapping newEmote = event.getOption("newemote");

    if (!checkOptions(
        event,
        name,
        regionName,
        isInVillage,
        newIsInVillage,
        villageName,
        newName,
        newVillageName,
        newRegionName)) return;

    PlaceEntity place = DatabaseManager.findByName(name, PlaceEntity.class);
    String placeVillageName =
        (place != null && place.getVillage() != null)
            ? place.getVillage().getNameWithoutEmote()
            : null;

    if (newName != null) {
      place.setName(newName.getAsString());
    }

    if (newDescription != null) {
      place.setDescription(newDescription.getAsString());
    }

    if (newEmote != null) {
      place.setEmote(newEmote.getAsString());
    }

    if (newRegionName != null) {
      place.setRegion(DatabaseManager.findByName(newRegionName.getAsString(), RegionEntity.class));
    }

    if (newIsInVillage) {
      place.setVillage(
          DatabaseManager.findByName(newVillageName.getAsString(), VillageEntity.class));
    }

    DatabaseManager.save(place);

    String formattedText = formatPlaceInformation(place);
    event.getHook().sendMessage(formattedText).queue();
  }

  private String formatPlaceInformation(PlaceEntity place) {
    return "╭───────────┈ ➤ ✎ **"
        + "\uD83C\uDF32 Place Information"
        + "**\n- "
        + "**Name:** `"
        + place.getName()
        + "`\n- "
        + "\uD83C\uDF67 **Emote:** "
        + place.getEmote()
        + "\n"
        + "\uD83C\uDF67 **Region:** "
        + place.getRegion().getName()
        + "\n"
        + "\uD83D\uDD16 **Village:** "
        + formatVillage(place)
        + "\n"
        + "\uD83D\uDC88 **Places:** "
        + formatPlace(place)
        + "\n╰─────────── ·\uFEFF \uFEFF \uFEFF· \uFEFF ·\uFEFF \uFEFF \uFEFF· \uFEFF✦";
  }

  private String formatPlace(PlaceEntity place) {
    return place.getRegion().getName();
  }

  private String formatVillage(PlaceEntity place) {
    return (place.getVillage() != null) ? place.getVillage().getName() : "None";
  }

  private boolean checkOptions(
      SlashCommandInteractionEvent event,
      String name,
      String regionName,
      boolean isInVillage,
      boolean newIsInVillage,
      OptionMapping villageName,
      OptionMapping newName,
      OptionMapping newVillageName,
      OptionMapping newRegionName) {
    if (!checkName(event, name, newName)) return false;
    else if (!checkRegion(event, regionName, newRegionName)) return false;
    else if (!checkCurrentVillage(event, villageName, isInVillage)) return false;
    else if (!checkNewVillage(event, newVillageName, newIsInVillage)) return false;

    if (!checkIfPlaceExist(name, regionName, isInVillage, villageName.getAsString())) {
      RedactorTextManager.sendErrorMessage(
          event, "Place doesn't exist.", "Please check your inputs.");
      return false;
    }

    if (newName != null && newRegionName != null && newIsInVillage && newVillageName != null) {
      if (checkIfPlaceExist(
          newName.getAsString(),
          newRegionName.getAsString(),
          newIsInVillage,
          newVillageName.getAsString())) {
        RedactorTextManager.sendErrorMessage(
            event, "Place already exists.", "Please specify a different place name.");
        return false;
      }
    }
    return true;
  }

  private boolean checkName(
      SlashCommandInteractionEvent event, String name, OptionMapping newName) {
    if (DatabaseManager.findByName(name, PlaceEntity.class) == null) {
      RedactorTextManager.sendErrorMessage(
          event, "Place doesn't exist.", "Please specify a different place name.");
    } else if (newName != null
        && DatabaseManager.findByName(newName.getAsString(), PlaceEntity.class) != null) {
      RedactorTextManager.sendErrorMessage(
          event, "Place already exists.", "Please specify a different place name.");
      return false;
    }
    return true;
  }

  private boolean checkRegion(
      SlashCommandInteractionEvent event, String regionName, OptionMapping newRegionName) {
    if (DatabaseManager.findByName(regionName, RegionEntity.class) == null) {
      RedactorTextManager.sendErrorMessage(
          event, "Region doesn't exist.", "Please specify a different region name.");
      return false;
    } else if (newRegionName != null
        && DatabaseManager.findByName(newRegionName.getAsString(), RegionEntity.class) == null) {
      RedactorTextManager.sendErrorMessage(
          event, "New Region doesn't exist.", "Please specify a different region name.");
      return false;
    }
    return true;
  }

  private boolean checkCurrentVillage(
      SlashCommandInteractionEvent event, OptionMapping villageName, boolean isInVillage) {
    return checkVillage(event, villageName, isInVillage, false);
  }

  private boolean checkNewVillage(
      SlashCommandInteractionEvent event, OptionMapping newVillageName, boolean newIsInVillage) {
    return checkVillage(event, newVillageName, newIsInVillage, true);
  }

  private boolean checkVillage(
      SlashCommandInteractionEvent event,
      OptionMapping villageName,
      boolean isInVillage,
      boolean isNew) {
    if ((isInVillage && villageName == null)
        || (isInVillage
            && DatabaseManager.findByName(villageName.getAsString(), VillageEntity.class)
                == null)) {
      sendErrorVillage(event, isNew);
      return false;
    }

    return true;
  }

  private void sendErrorVillage(SlashCommandInteractionEvent event, boolean isNew) {
    if (isNew) {
      RedactorTextManager.sendErrorMessage(
          event, "New Village doesn't exist.", "Please specify a different village name.");
    } else {
      RedactorTextManager.sendErrorMessage(
          event, "Village doesn't exist.", "Please specify a village name.");
    }
  }

  private boolean checkIfPlaceExist(
      String name, String region, boolean isInVillage, String villageName) {
    PlaceEntity place = DatabaseManager.findByName(name, PlaceEntity.class);
    String placeVillageName =
        (place != null && place.getVillage() != null)
            ? place.getVillage().getNameWithoutEmote()
            : null;

    return place != null
        && (place.getRegion().getNameWithoutEmote().equals(region)
            || (!Objects.equals(placeVillageName, villageName) || !isInVillage));
  }
}
