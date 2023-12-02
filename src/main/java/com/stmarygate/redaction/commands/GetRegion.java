package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.PlaceEntity;
import com.stmarygate.redaction.entities.RegionEntity;
import com.stmarygate.redaction.entities.VillageEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class GetRegion extends CommandAbstract {
  public GetRegion(StMaryRedactor client) {
    super(client);

    this.name = "getregion";
    this.description = "Get a region.";

    this.options.add(
        new OptionData(OptionType.STRING, "name", "The name of the region to get.", true));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    String name = event.getOption("name").getAsString();
    RegionEntity region = DatabaseManager.findByName(name, RegionEntity.class);

    if (region == null) {
      EmbedBuilder embed = new EmbedBuilder();
      embed.setAuthor("Region doesn't exist!", null, event.getUser().getAvatarUrl());
      embed.setDescription("A region with the same name doesn't exist.");
      embed.addField("Name", name, true);
      embed.setColor(0xff0000);
      embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
      embed.setTimestamp(event.getTimeCreated());

      event.getHook().sendMessageEmbeds(embed.build()).queue();
    } else {
      String formattedText =
          "╭───────────┈ ➤ ✎ **"
              + "\uD83C\uDF0D Region Information"
              + "**\n- "
              + "**Name:** `"
              + region.getName()
              + "`\n- "
              + "\uD83C\uDF67 **Description:** "
              + region.getDescription()
              + "\n"
              + "\uD83D\uDD16 **Villages:** "
              + formatVillages(region)
              + "\n"
              + "\uD83D\uDC88 **Places:** "
              + formatPlaces(region)
              + "\n╰─────────── ·\uFEFF \uFEFF \uFEFF· \uFEFF ·\uFEFF \uFEFF \uFEFF· \uFEFF✦";

      event.getHook().sendMessage(formattedText).queue();
    }
  }

  private String formatVillages(RegionEntity region) {
    StringBuilder formattedText = new StringBuilder();
    for (VillageEntity village : region.getVillages()) {
      formattedText.append("- \uD83C\uDFD8️ `").append(village.getName()).append("`\n");
    }
    return formattedText.toString();
  }

  private String formatPlaces(RegionEntity region) {
    StringBuilder formattedText = new StringBuilder();
    for (PlaceEntity place : region.getPlaces()) {
      formattedText.append("- \uD83D\uDCCD `").append(place.getName()).append("`\n");
    }
    return formattedText.toString();
  }
}
