package net.likelion.bebc25.fixgo.repair.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Repair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long as_request_id;

    private Long technician_id;

    private String diagnosis;

    private String work_note;

    private String repair_decision;

    @Column(nullable = false)
    private String warranty_result;

    private String reason;

    private int labor_rate;

    private int inspection_cost;

    private int final_amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepairStatus status;

    private String reason_for_rejection;

    private LocalDateTime confirmed_at;

    @Column(nullable = false)
    private LocalDateTime created_at;

    @Column(nullable = false)
    private LocalDateTime updated_at;

    @Column(nullable = false)
    private String created_by;

    @Column(nullable = false)
    private String updated_by;
}
