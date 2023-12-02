package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.PlaceEntity;
import com.stmarygate.redaction.entities.VillageEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class GetVillage extends CommandAbstract {
  public GetVillage(StMaryRedactor client) {
    super(client);

    this.name = "getvillage";
    this.description = "Get a village.";
    this.options.add(
        new OptionData(OptionType.STRING, "name", "The name of the village to get.", true));
    this.options.add(
        new OptionData(OptionType.STRING, "region", "The region of the village to get.", true));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    String name = event.getOption("name").getAsString();

    event.deferReply().queue();

    VillageEntity village = DatabaseManager.findByName(name, VillageEntity.class);

    if (village == null
        || !village
            .getRegion()
            .getNameWithoutEmote()
            .equals(event.getOption("region").getAsString())) {
      EmbedBuilder embed = new EmbedBuilder();
      embed.setAuthor("Village does not exist!", null, event.getUser().getAvatarUrl());
      embed.setDescription("The village you specified does not exist.");
      embed.addField("Village", name, true);
      embed.addField("Region", event.getOption("region").getAsString(), true);
      embed.setColor(0xff0000);
      embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
      embed.setTimestamp(event.getTimeCreated());

      event.getHook().sendMessageEmbeds(embed.build()).queue();
    } else {
      String formattedText =
          "╭───────────┈ ➤ ✎ **"
              + "\uD83C\uDF32 Place Information"
              + "**\n- "
              + "**Name:** `"
              + village.getName()
              + "`\n- "
              + "\uD83C\uDF67 **Description:** "
              + village.getDescription()
              + "\n"
              + "**Region:** "
              + village.getRegion().getName()
              + "\n"
              + "\uD83D\uDC88 **Places:** "
              + formatPlace(village)
              + "\n╰─────────── ·\uFEFF \uFEFF \uFEFF· \uFEFF ·\uFEFF \uFEFF \uFEFF· \uFEFF✦";

      event.getHook().sendMessage(formattedText).queue();
    }
  }

  private String formatPlace(VillageEntity village) {
    StringBuilder formattedText = new StringBuilder();
    for (PlaceEntity place : village.getPlaces()) {
      formattedText.append("- \uD83C\uDFD8️ `").append(place.getName()).append("`\n");
    }
    return formattedText.toString();
  }
}
