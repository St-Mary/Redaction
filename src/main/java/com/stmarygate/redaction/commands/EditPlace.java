package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.Constants;
import com.stmarygate.redaction.bot.RedactorTextManager;
import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.PlaceEntity;
import com.stmarygate.redaction.entities.RegionEntity;
import com.stmarygate.redaction.entities.VillageEntity;
import java.util.Objects;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

public class EditPlace extends CommandAbstract {

  Name namePlace;
  Description descriptionPlace;
  Village villagePlace;
  Region regionPlace;
  Emote emotePlace;

  PlaceEntity place;
  String currentName;
  String currentDescription;
  String currentVillage;
  String currentRegion;
  String currentEmote;
  boolean isInVillage;

  public EditPlace(StMaryRedactor client) {
    super(client);
    this.name = "editplace";
    this.description = "Edit a place.";

    this.options.add(new OptionData(OptionType.STRING, "place", "The place to get.", true));
    this.options.add(
        new OptionData(OptionType.STRING, "region", "The region of the place to get.", true));
    this.options.add(
        new OptionData(OptionType.BOOLEAN, "isinvillage", "If the place is in a village", true));
    this.options.add(
        new OptionData(OptionType.STRING, "village", "The village of the place to get.", false));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    currentName = Objects.requireNonNull(event.getOption("place")).getAsString();
    currentRegion = Objects.requireNonNull(event.getOption("region")).getAsString();
    isInVillage = Objects.requireNonNull(event.getOption("isinvillage")).getAsBoolean();
    currentVillage =
        event.getOption("village") != null
            ? Objects.requireNonNull(event.getOption("village")).getAsString()
            : null;

    event.getJDA().addEventListener(new ModalListener(this));
    event.replyModal(getModal()).queue();
  }

  private Modal getModal() {
    TextInput name =
        TextInput.create("name", "Name", TextInputStyle.SHORT)
            .setPlaceholder("Enter the new name (Enter \"none\" to keep the current name)")
            .setRequired(true)
            .build();

    TextInput description =
        TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
            .setPlaceholder(
                "Enter the new description (Enter \"none\" to keep the current description)")
            .setRequired(true)
            .build();

    TextInput village =
        TextInput.create("village", "Village", TextInputStyle.SHORT)
            .setPlaceholder("Enter the new village (Enter \"none\" to keep the current village)")
            .setRequired(true)
            .build();

    TextInput region =
        TextInput.create("region", "Region", TextInputStyle.SHORT)
            .setPlaceholder("Enter the new region (Enter \"none\" to keep the current region)")
            .setRequired(true)
            .build();

    TextInput emote =
        TextInput.create("emote", "Emote", TextInputStyle.SHORT)
            .setPlaceholder("Enter the new emote (Enter \"none\" to keep the current emote)")
            .setRequired(true)
            .build();

    return Modal.create("newinformations", "New Place informations")
        .addActionRow(name)
        .addActionRow(description)
        .addActionRow(village)
        .addActionRow(region)
        .addActionRow(emote)
        .build();
  }

  public class ModalListener extends ListenerAdapter {
    EditPlace editPlace;

    public ModalListener(EditPlace editPlace) {
      this.editPlace = editPlace;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
      if (event.getModalId().equals("newinformations")) {
        event.deferReply().queue();
        place = DatabaseManager.findByName(editPlace.currentName, PlaceEntity.class);

        if (place == null) {
          event
              .getHook()
              .sendMessage(
                  "This place doesn't exist. Please consider to modify your command parameters")
              .queue();
          return;
        }
        String placeVillageName =
            (place.getVillage() != null) ? place.getVillage().getNameWithoutEmote() : null;

        if (!place.getRegion().getNameWithoutEmote().equals(editPlace.currentRegion)
            && (Objects.equals(placeVillageName, editPlace.currentVillage)
                && editPlace.isInVillage)) {
          event
              .getHook()
              .sendMessage(
                  "This place doesn't exist. Please consider to modify your command parameters")
              .queue();
        }

        currentDescription = place.getDescription();
        currentEmote = place.getEmote();

        String name = event.getValue("name").getAsString();
        String description = event.getValue("description").getAsString();
        String village = event.getValue("village").getAsString();
        String region = event.getValue("region").getAsString();
        String emote = event.getValue("emote").getAsString();

        editPlace.namePlace = new Name(editPlace.currentName, name);
        editPlace.descriptionPlace = new Description(editPlace.currentDescription, description);
        editPlace.villagePlace = new Village(editPlace.currentVillage, village);
        editPlace.regionPlace = new Region(editPlace.currentRegion, region);
        editPlace.emotePlace = new Emote(editPlace.currentEmote, emote);

        if (!checkVillageAndRegion(event)) return;

        place.setName(editPlace.namePlace.getNewName());
        place.setDescription(editPlace.descriptionPlace.getNewDescription());
        place.setVillage(
            DatabaseManager.findByName(
                editPlace.villagePlace.getNewVillage(), VillageEntity.class));
        place.setRegion(
            DatabaseManager.findByName(editPlace.regionPlace.getNewRegion(), RegionEntity.class));
        place.setEmote(editPlace.emotePlace.getNewEmote());

        DatabaseManager.save(place);

        String formattedText = formatPlaceInformation(place);
        event.getHook().sendMessage(formattedText).queue();

        event.getJDA().removeEventListener(this);
      }
    }

    private String formatPlaceInformation(PlaceEntity place) {
      return "╭───────────┈ ➤ ✎ **"
          + Constants.PLACE_EMOJI
          + " New Place Information"
          + "**\n- "
          + "**Name:** `"
          + place.getName()
          + "`\n- "
          + Constants.DESCRIPTION_EMOJI
          + " **Description:** "
          + place.getDescription()
          + "\n"
          + Constants.VILLAGE_EMOJI
          + " **Village:** "
          + place.getVillage().getName()
          + "\n"
          + Constants.REGION_EMOJI
          + " **Region:** "
          + place.getRegion().getName()
          + "\n"
          + Constants.PLACE_EMOJI
          + " **Emote:** "
          + place.getEmote()
          + "\n╰─────────── ·\uFEFF \uFEFF \uFEFF· \uFEFF ·\uFEFF \uFEFF \uFEFF· \uFEFF✦";
    }

    private boolean checkVillageAndRegion(ModalInteractionEvent event) {
      VillageEntity village =
          DatabaseManager.findByName(editPlace.villagePlace.getNewVillage(), VillageEntity.class);
      RegionEntity region =
          DatabaseManager.findByName(editPlace.regionPlace.getNewRegion(), RegionEntity.class);

      if (village == null) {
        RedactorTextManager.sendErrorMessage(
            event, "This village doesn't exist.", "Please specify a different new village name.");
        return false;
      }

      if (region == null) {
        RedactorTextManager.sendErrorMessage(
            event, "This region doesn't exist.", "Please specify a different new region name.");
        return false;
      }

      return true;
    }
  }

  private class Name {
    @Getter private final String currentName;
    private final String newName;
    @Getter private final boolean isNameChanged;

    public Name(String currentName, String newName) {
      this.currentName = currentName;
      this.newName = newName;
      this.isNameChanged = !newName.equals("\"none\"");
    }

    public String getNewName() {
      return (isNameChanged) ? newName : currentName;
    }
  }

  private class Description {
    @Getter private final String currentDescription;
    private final String newDescription;
    @Getter private final boolean isDescriptionChanged;

    public Description(String currentDescription, String newDescription) {
      this.currentDescription = currentDescription;
      this.newDescription = newDescription;
      this.isDescriptionChanged = !newDescription.equals("\"none\"");
    }

    public String getNewDescription() {
      return (isDescriptionChanged) ? newDescription : currentDescription;
    }
  }

  private class Village {
    @Getter private final String currentVillage;
    private final String newVillage;
    @Getter private final boolean isVillageChanged;

    public Village(String currentVillage, String newVillage) {
      this.currentVillage = currentVillage;
      this.newVillage = newVillage;
      this.isVillageChanged = !newVillage.equals("\"none\"");
    }

    public String getNewVillage() {
      return (isVillageChanged) ? newVillage : currentVillage;
    }
  }

  private class Region {
    @Getter private final String currentRegion;
    private final String newRegion;
    @Getter private final boolean isRegionChanged;

    public Region(String currentRegion, String newRegion) {
      this.currentRegion = currentRegion;
      this.newRegion = newRegion;
      this.isRegionChanged = !newRegion.equals("\"none\"");
    }

    public String getNewRegion() {
      return (isRegionChanged) ? newRegion : currentRegion;
    }
  }

  private class Emote {
    @Getter private final String currentEmote;
    private final String newEmote;
    @Getter private final boolean isEmoteChanged;

    public Emote(String currentEmote, String newEmote) {
      this.currentEmote = currentEmote;
      this.newEmote = newEmote;
      this.isEmoteChanged = !newEmote.equals("\"none\"");
    }

    public String getNewEmote() {
      return (isEmoteChanged) ? newEmote : currentEmote;
    }
  }
}
