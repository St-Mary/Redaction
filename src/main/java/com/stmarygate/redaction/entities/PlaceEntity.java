package com.stmarygate.redaction.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "places")
public class PlaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "place_name", nullable = false)
    private String name;

    @Setter
    @Column(name = "place_description", nullable = false)
    private String description;

    @Setter
    @Column(name = "place_village", nullable = true)
    @ManyToOne
    private VillageEntity village;

    @Setter
    @Column(name = "place_region", nullable = true)
    @ManyToOne
    private RegionEntity region;
}