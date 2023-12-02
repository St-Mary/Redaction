package com.stmarygate.redaction.bot;

import com.stmarygate.redaction.Constants;
import com.stmarygate.redaction.commands.*;
import com.stmarygate.redaction.database.DatabaseManager;
import java.util.ArrayList;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class StMaryRedactor {

  private final JDA jda;
  private final ArrayList<CommandAbstract> commands = new ArrayList<>();

  public StMaryRedactor() {
    EventListener eventListener = new EventListener(this);
    DatabaseManager.getSessionFactory();
    jda = JDABuilder.createDefault(Constants.getToken()).addEventListeners(eventListener).build();

    commands.add(new AddVillage(this));
    commands.add(new AddRegion(this));
    commands.add(new AddPlace(this));
    commands.add(new GetRegion(this));
    commands.add(new GetVillage(this));
    commands.add(new GetPlace(this));
    commands.add(new GetRegions(this));
  }

  private static class EventListener extends ListenerAdapter {
    private final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);
    private final StMaryRedactor stMaryRedactor;

    public EventListener(StMaryRedactor stMaryRedactor) {
      this.stMaryRedactor = stMaryRedactor;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
      LOGGER.info("StMaryRedactor - Ready.", event.getJDA().getSelfUser().getGlobalName());
      TextChannel chan = event.getJDA().getTextChannelById(Constants.getLogChannel());
      chan.sendMessage(":fire: `StMaryRedactor` - Ready.").queue();

      // Register slash commands to specific guilds.
      for (CommandAbstract command : stMaryRedactor.getCommands()) {
        chan.getGuild().upsertCommand(command.buildCommandData()).queue();
      }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
      LOGGER.info(
          "Slash command received: "
              + event.getName()
              + " from "
              + event.getUser().getGlobalName());

      for (CommandAbstract command : stMaryRedactor.getCommands()) {
        if (command.getName().equals(event.getName())) {
          command.run(event);
          break;
        }
      }
    }
  }
}
