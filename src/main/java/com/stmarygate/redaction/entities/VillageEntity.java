package com.stmarygate.redaction.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Entity
@Table(name = "villages")
public class VillageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "village_name", nullable = false)
    private String name;

    @Setter
    @Column(name = "village_description", nullable = false)
    private String description;

    // Array of places in the village, so array of PlaceEntity
    @Setter
    @Column(name = "village_places", nullable = false)
    @OneToMany(mappedBy = "village")
    private List<PlaceEntity> places;

    @Setter
    @JoinColumn(name = "village_region", nullable = true)
    @ManyToOne
    private RegionEntity region;
}
