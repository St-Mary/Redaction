package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.StMaryRedactor;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CommandAbstract {

  protected final StMaryRedactor client;
  protected final List<OptionData> options = new ArrayList<>();
  private final Logger LOGGER = LoggerFactory.getLogger(CommandAbstract.class);

  /** Get the name of the command. */
  @Getter protected String name;

  /** Get the description of the command. */
  @Getter protected String description;

  /**
   * Constructor for the AbstractCommand class
   *
   * @param client The client instance
   */
  protected CommandAbstract(StMaryRedactor client) {
    this.client = client;
    this.name = this.getClass().getSimpleName().toLowerCase();
    this.description = "No description provided.";
  }

  /**
   * Execute the command
   *
   * @param event The SlashCommandInteractionEvent triggered when the button is clicked.
   */
  public abstract void execute(SlashCommandInteractionEvent event);

  /**
   * Run the command
   *
   * @param event The SlashCommandInteractionEvent triggered when the button is clicked.
   */
  public void run(SlashCommandInteractionEvent event) {
    try {
      this.execute(event);
    } catch (Exception e) {
      LOGGER.error("Error while executing command: ");
      e.printStackTrace();
    }
  }

  /**
   * Send a message with buttons
   *
   * @return The SlashCommandData of the command
   */
  public SlashCommandData buildCommandData() {
    SlashCommandData data = Commands.slash(this.name, this.description);

    if (!this.options.isEmpty()) {
      data.addOptions(this.options);
    }
    return data;
  }
}
