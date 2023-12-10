package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.Constants;
import com.stmarygate.redaction.bot.RedactorTextManager;
import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.Location;
import com.stmarygate.redaction.entities.RegionEntity;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class GetRegion extends CommandAbstract {

  public GetRegion(StMaryRedactor client) {
    super(client);
    this.name = "getregion";
    this.description = "Get a region";
    this.options.add(
        new OptionData(OptionType.STRING, "name", "The name of the region to get.", true));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping nameOption = event.getOption("name");
    if (nameOption == null || !nameOption.getType().equals(OptionType.STRING)) {
      RedactorTextManager.sendErrorMessage(
          event, "Invalid input for region name.", "Please specify a valid region name.");
      return;
    }
    String name = nameOption.getAsString();

    RegionEntity region = findRegionByName(name);
    if (region == null) {
      sendRegionNotFound(event, name);
      return;
    }

    String formattedText = formatRegionInformation(region);
    sendMessage(event, formattedText);
  }

  private RegionEntity findRegionByName(String name) {
    return DatabaseManager.findByName(name, RegionEntity.class);
  }

  private String formatRegionInformation(RegionEntity region) {
    return "╭───────────┈ ➤ ✎ **"
        + Constants.REGION_EMOJI
        + " Region Information"
        + "**\n- "
        + "**Name:** `"
        + region.getName()
        + "`\n- "
        + Constants.DESCRIPTION_EMOJI
        + " **Description:** "
        + region.getDescription()
        + "\n"
        + "\uD83D\uDD16 **Villages:** "
        + formatEntities(region.getVillages(), Constants.VILLAGE_EMOJI)
        + "\n"
        + "\uD83D\uDC88 **Places:** "
        + formatEntities(region.getPlaces(), Constants.PLACE_EMOJI)
        + "\n"
        + "╰─────────── ·\uFEFF \uFEFF \uFEFF· \uFEFF ·\uFEFF \uFEFF \uFEFF· \uFEFF✦";
  }

  private <T extends Location> String formatEntities(List<T> entities, String emoji) {
    StringBuilder formattedText = new StringBuilder();
    for (T entity : entities) {
      formattedText.append("- ").append(emoji).append(" `").append(entity.getName()).append("`\n");
    }
    return formattedText.toString();
  }

  private void sendRegionNotFound(SlashCommandInteractionEvent event, String regionName) {
    RedactorTextManager.sendErrorMessage(
        event,
        "Region doesn't exist!",
        "A region with the name '" + regionName + "' doesn't exist.");
  }

  private void sendMessage(SlashCommandInteractionEvent event, String message) {
    event.getHook().sendMessage(message).queue();
  }
}
