package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.Constants;
import com.stmarygate.redaction.bot.RedactorTextManager;
import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.RegionEntity;
import java.util.Objects;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class EditRegion extends CommandAbstract {
  public EditRegion(StMaryRedactor client) {
    super(client);

    this.name = "editregion";
    this.description = "Edit a region.";

    this.options.add(
        new OptionData(OptionType.STRING, "name", "The name of the region to edit.", true));
    this.options.add(
        new OptionData(OptionType.STRING, "emote", "The emote of the region to edit.", false));
    this.options.add(
        new OptionData(
            OptionType.STRING, "description", "The description of the region to edit.", false));
    this.options.add(
        new OptionData(OptionType.STRING, "newname", "The new name of the region to edit.", false));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    String name = Objects.requireNonNull(event.getOption("name")).getAsString();
    OptionMapping newName = event.getOption("newname");
    OptionMapping emoteOption = event.getOption("emote");
    OptionMapping descriptionOption = event.getOption("description");

    RegionEntity region = DatabaseManager.findByName(name, RegionEntity.class);
    if (region == null) {
      RedactorTextManager.sendRegionNotFoundError(event, name);
      return;
    }

    if (newName != null
        && DatabaseManager.findByName(newName.getAsString(), RegionEntity.class) != null) {
      RedactorTextManager.sendErrorMessage(
          event, "Region already exists.", "Please specify a different region name.");
      return;
    }

    if (newName != null) {
      region.setName(newName.getAsString());
    }

    if (emoteOption != null) {
      region.setEmote(emoteOption.getAsString());
    }

    if (descriptionOption != null) {
      region.setDescription(descriptionOption.getAsString());
    }

    DatabaseManager.save(region);

    String formattedText = formatRegionInformation(region);
    event.getHook().sendMessage(formattedText).queue();
  }

  private String formatRegionInformation(RegionEntity region) {
    return "╭───────────┈ ➤ ✎ **"
        + Constants.REGION_EMOJI
        + " Region Information"
        + "**\n- "
        + "**Name:** `"
        + region.getName()
        + "`\n- "
        + Constants.REGION_EMOJI
        + " **Emote:** "
        + region.getEmote()
        + "\n"
        + Constants.DESCRIPTION_EMOJI
        + " **Description:** "
        + region.getDescription()
        + "\n"
        + Constants.VILLAGE_EMOJI
        + " **Villages:** "
        + formatVillages(region)
        + "\n"
        + Constants.PLACE_EMOJI
        + " **Places:** "
        + formatPlaces(region)
        + "\n"
        + "╰───────────┈ ➤ ✎";
  }

  private String formatVillages(RegionEntity region) {
    StringBuilder formattedText = new StringBuilder();
    for (int i = 0; i < region.getVillages().size(); i++) {
      formattedText.append("- ").append(region.getVillages().get(i).getName()).append("\n");
    }
    return formattedText.toString();
  }

  private String formatPlaces(RegionEntity region) {
    StringBuilder formattedText = new StringBuilder();
    for (int i = 0; i < region.getPlaces().size(); i++) {
      formattedText.append("- ").append(region.getPlaces().get(i).getName()).append("\n");
    }
    return formattedText.toString();
  }
}
