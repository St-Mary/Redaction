package com.stmarygate.redaction.commands;

import com.stmarygate.redaction.bot.RedactorTextManager;
import com.stmarygate.redaction.bot.StMaryRedactor;
import com.stmarygate.redaction.database.DatabaseManager;
import com.stmarygate.redaction.entities.PlaceEntity;
import com.stmarygate.redaction.entities.RegionEntity;
import com.stmarygate.redaction.entities.VillageEntity;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

public class EditVillage extends CommandAbstract {

  private VillageEntity village;

  public EditVillage(StMaryRedactor client) {
    super(client);

    this.name = "editvillage";
    this.description = "Edit a village.";

    this.options.add(
        new OptionData(OptionType.STRING, "name", "The name of the village to edit.", true));
    this.options.add(
        new OptionData(OptionType.STRING, "region", "The region of the village to edit.", true));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    String nameVillage = event.getOption("name").getAsString();
    String nameRegion = event.getOption("region").getAsString();

    village = DatabaseManager.findByName(nameVillage, VillageEntity.class);
    if (village == null || !village.getRegion().getNameWithoutEmote().equals(nameRegion)) {
      RedactorTextManager.sendVillageNotFoundError(event, nameVillage);
      return;
    }

    event.getJDA().addEventListener(new ModalListener(this, event.getUser().getId()));
    event.replyModal(getModal()).queue();
  }

  private Modal getModal() {
    TextInput nameInput =
        TextInput.create("name", "Enter the new name of the village.", TextInputStyle.SHORT)
            .setRequired(true)
            .setMinLength(1)
            .setPlaceholder("Enter \"none\" to not change the name.")
            .build();

    TextInput regionInput =
        TextInput.create("region", "Enter the new region of the village.", TextInputStyle.SHORT)
            .setRequired(true)
            .setMinLength(1)
            .setPlaceholder("Enter \"none\" to not change the region.")
            .build();

    TextInput descriptionInput =
        TextInput.create(
                "description",
                "Enter the new description of the village.",
                TextInputStyle.PARAGRAPH)
            .setRequired(true)
            .setMinLength(1)
            .setPlaceholder("Enter \"none\" to not change the description.")
            .build();

    TextInput emoteInput =
        TextInput.create("emote", "Enter the new emote of the village.", TextInputStyle.SHORT)
            .setRequired(true)
            .setMinLength(1)
            .setPlaceholder("Enter \"none\" to not change the emote.")
            .build();

    return Modal.create("villageinformations", "Village Informations")
        .addActionRow(nameInput)
        .addActionRow(regionInput)
        .addActionRow(descriptionInput)
        .addActionRow(emoteInput)
        .build();
  }

  private String formatVillageInformation(VillageEntity village) {
    return "╭───────────┈ ➤ ✎ **"
        + "\uD83C\uDF32 Village Information"
        + "**\n- "
        + "**Name:** `"
        + village.getName()
        + "`\n- "
        + "\uD83C\uDF67 **Description:** "
        + village.getDescription()
        + "\n"
        + "\uD83D\uDD16 **Region:** "
        + village.getRegion().getName()
        + "\n"
        + "\uD83D\uDC88 **Places:** "
        + formatPlace(village)
        + "\n╰─────────── ·\uFEFF \uFEFF \uFEFF· \uFEFF ·\uFEFF \uFEFF \uFEFF· \uFEFF✦";
  }

  private String formatPlace(VillageEntity village) {
    StringBuilder formattedText = new StringBuilder();
    for (PlaceEntity place : village.getPlaces()) {
      formattedText.append("- \uD83C\uDFD8️ `").append(place.getName()).append("`\n");
    }
    return formattedText.toString();
  }

  private boolean isRegionValid(String regionName) {
    return DatabaseManager.findByName(regionName, RegionEntity.class) != null;
  }

  public class ModalListener extends ListenerAdapter {

    private final EditVillage editVillage;
    private final String authorId;

    public ModalListener(EditVillage editVillage, String authorId) {
      this.editVillage = editVillage;
      this.authorId = authorId;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
      if (event.getModalId().equals("villageinformations")
          && event.getUser().getId().equals(authorId)) {
        event.deferReply().queue();
        String name = event.getValue("name").getAsString();
        String regionName = event.getValue("region").getAsString();
        String description = event.getValue("description").getAsString();
        String emote = event.getValue("emote").getAsString();

        Name nameObject = new Name(editVillage.village.getNameWithoutEmote(), name);
        Region regionObject =
            new Region(editVillage.village.getRegion().getNameWithoutEmote(), regionName);
        Description descriptionObject =
            new Description(editVillage.village.getDescription(), description);
        Emote emoteObject = new Emote(editVillage.village.getEmote(), emote);

        if (!isRegionValid(regionObject.getNewRegion())) {
          EmbedBuilder embed = new EmbedBuilder();
          embed.setAuthor("Region does not exist!", null, event.getUser().getAvatarUrl());
          embed.setDescription("The region you specified does not exist.");
          embed.addField("Region", regionObject.getNewRegion(), true);
          embed.setColor(0xff0000);
          embed.setFooter("StMaryRedactor", event.getJDA().getSelfUser().getAvatarUrl());
          embed.setTimestamp(event.getTimeCreated());

          event.getHook().sendMessageEmbeds(embed.build()).queue();
          event.getJDA().removeEventListener(this);
          return;
        }

        editVillage.village.setName(nameObject.getNewName());
        editVillage.village.setRegion(
            DatabaseManager.findByName(regionObject.getNewRegion(), RegionEntity.class));
        editVillage.village.setDescription(descriptionObject.getNewDescription());
        editVillage.village.setEmote(emoteObject.getNewEmote());

        DatabaseManager.save(editVillage.village);

        String formattedText = formatVillageInformation(editVillage.village);

        event.getHook().sendMessage(formattedText).queue();
      }
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
