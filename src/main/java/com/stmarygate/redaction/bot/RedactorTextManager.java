package com.stmarygate.redaction.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RedactorTextManager {
  public static void sendErrorMessage(
      SlashCommandInteractionEvent event, String title, String description) {
    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor(title, null, event.getUser().getAvatarUrl());
    embed.setDescription(description);
    embed.setColor(0xff0000);
    embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
    embed.setTimestamp(event.getTimeCreated());

    event.getHook().sendMessageEmbeds(embed.build()).queue();
  }

  public static void sendRegionNotFoundError(SlashCommandInteractionEvent event, String name) {
    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Region does not exist!", null, event.getUser().getAvatarUrl());
    embed.setDescription("The region you specified does not exist.");
    embed.addField("Region", name, true);
    embed.setColor(0xff0000);
    embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
    embed.setTimestamp(event.getTimeCreated());

    event.getHook().sendMessageEmbeds(embed.build()).queue();
  }
}
