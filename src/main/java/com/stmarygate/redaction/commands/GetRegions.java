package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.RegionEntity;
import java.awt.*;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class GetRegions extends CommandAbstract {
  public GetRegions(StMaryRedactor client) {
    super(client);

    this.name = "getregions";
    this.description = "Get all regions.";
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    List<RegionEntity> regions = DatabaseManager.findAll(RegionEntity.class);

    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Regions", null, event.getUser().getAvatarUrl());
    embed.setColor(Color.getColor("#ff8e00"));
    embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
    embed.setTimestamp(event.getTimeCreated());

    StringBuilder description = new StringBuilder("All regions:\n");
    for (RegionEntity region : regions) {
      description.append(region.getName()).append("\n");
    }

    embed.setDescription(description.toString());

    event.getHook().sendMessageEmbeds(embed.build()).queue();
  }
}
