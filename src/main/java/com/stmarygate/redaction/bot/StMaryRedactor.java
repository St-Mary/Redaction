package com.stmarygate.redaction.bot;

import com.stmarygate.redaction.Constants;
import com.stmarygate.redaction.commands.*;
import com.stmarygate.redaction.database.DatabaseManager;
import java.util.ArrayList;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class StMaryRedactor {

  private final Logger LOGGER = LoggerFactory.getLogger(StMaryRedactor.class);
  private final JDA jda;
  private final ArrayList<CommandAbstract> commands = new ArrayList<>();
  private TextChannel logChannel;

  public StMaryRedactor() throws InterruptedException {
    initializeCommands();
    EventListener eventListener = new EventListener(this);
    DatabaseManager.getSessionFactory();
    jda = JDABuilder.createDefault(Constants.getToken()).addEventListeners(eventListener).build();
  }

  private void initializeCommands() {
    commands.add(new AddVillage(this));
    commands.add(new AddRegion(this));
    commands.add(new AddPlace(this));
    commands.add(new EditRegion(this));
    commands.add(new EditPlace(this));
    commands.add(new EditVillage(this));
    commands.add(new GetRegion(this));
    commands.add(new GetVillage(this));
    commands.add(new GetPlace(this));
    commands.add(new GetRegions(this));
  }

  private void registerSlashCommands() {
    for (CommandAbstract command : commands) {
      logChannel.getGuild().upsertCommand(command.buildCommandData()).queue();
    }
  }

  private void checkLogChannel() {
    if (logChannel == null || logChannel.getType().equals(ChannelType.PRIVATE)) {
      LOGGER.error("Log channel is not set or is a private channel: {}", logChannel);
      System.exit(1);
    }
  }

  private static class EventListener extends ListenerAdapter {
    private final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);
    private final StMaryRedactor stMaryRedactor;

    public EventListener(StMaryRedactor stMaryRedactor) {
      this.stMaryRedactor = stMaryRedactor;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
      LOGGER.info("StMaryRedactor - Ready. Bot Name: {}", event.getJDA().getSelfUser().getName());
      this.stMaryRedactor.logChannel = event.getJDA().getTextChannelById(Constants.getLogChannel());
      this.stMaryRedactor.checkLogChannel();
      this.stMaryRedactor.registerSlashCommands();
      stMaryRedactor.getLogChannel().sendMessage(":fire: `StMaryRedactor` - Ready.").queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
      LOGGER.info(
          "Slash command received: "
              + event.getName()
              + " from "
              + event.getUser().getGlobalName());

      stMaryRedactor.getCommands().stream()
          .filter(command -> command.getName().equals(event.getName()))
          .findFirst()
          .ifPresent(command -> command.run(event));
    }
  }
}
