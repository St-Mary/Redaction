package com.stmarygate.redaction.entities;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "villages")
public class VillageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(name = "village_emote", nullable = false)
  private String emote;

  @Setter
  @Column(name = "village_name", nullable = false)
  private String name;

  @Setter
  @Column(name = "village_description", nullable = false, length = 8192)
  private String description;

  // Array of places in the village, so array of PlaceEntity
  @Setter
  @Column(name = "village_places", nullable = false)
  @OneToMany(mappedBy = "village", fetch = FetchType.EAGER)
  private List<PlaceEntity> places;

  @Setter
  @JoinColumn(name = "village_region", nullable = true)
  @ManyToOne
  private RegionEntity region;

  public String getName() {
    return this.emote + " " + this.name;
  }

  public String getNameWithoutEmote() {
    return this.name;
  }
}
