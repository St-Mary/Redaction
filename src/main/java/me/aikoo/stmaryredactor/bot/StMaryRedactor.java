package me.aikoo.stmaryredactor.bot;

import lombok.Getter;
import me.aikoo.stmaryredactor.Constants;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StMaryRedactor {

    @Getter private final JDA jda;
    private final EventListener eventListener = new EventListener(this);

    public StMaryRedactor() {
        jda = JDABuilder.createDefault(Constants.getToken())
                .addEventListeners(eventListener)
                .build();
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
            event.getJDA().getTextChannelById(Constants.getLogChannel()).sendMessage(":fire: `StMaryRedactor` - Ready.").queue();

            // Register slash commands
            stMaryRedactor.getJda().upsertCommand("ping", "Ping the bot.").queue();
        }

        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
            LOGGER.info("StMaryRedactor - Slash Command Interaction.", event.getJDA().getSelfUser().getGlobalName());
            event.reply("Pong!").queue();
        }
    }
}
