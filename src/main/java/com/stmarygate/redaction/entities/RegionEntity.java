package com.stmarygate.redaction.entities;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "regions")
public class RegionEntity extends Location {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(name = "region_emote", nullable = false)
  private String emote;

  @Setter
  @Column(name = "region_name", nullable = false)
  private String name;

  @Setter
  @Column(name = "region_description", nullable = false, length = 8192)
  private String description;

  @Setter
  @Column(name = "region_places", nullable = true)
  @OneToMany(mappedBy = "region", fetch = FetchType.EAGER)
  private List<PlaceEntity> places;

  @Setter
  @Column(name = "region_villages", nullable = true)
  @OneToMany(mappedBy = "region", fetch = FetchType.EAGER)
  private List<VillageEntity> villages;

  public String getName() {
    return this.emote + " " + this.name;
  }

  public String getNameWithoutEmote() {
    return this.name;
  }
}
