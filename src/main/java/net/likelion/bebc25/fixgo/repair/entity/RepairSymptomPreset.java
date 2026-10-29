package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;

public class RepairSymptomPreset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_id", nullable = false)
    private Repair repair;

    @Column(nullable = false)
    private String sysptomPresetName;
}
