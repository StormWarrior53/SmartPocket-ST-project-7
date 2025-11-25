package org.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @Column(nullable = false)
    private String name;

    private int age;

    @Column(nullable = false)
    private String pinHash; // PIN acts as the child's password

    // Gamification fields
    @Builder.Default
    private int xp = 0;

    @Builder.Default
    private int pocketMoney = 0;

    @Builder.Default
    private int allowanceMoney = 0;

    // Future relationships:
    // List<Enrollment> enrollments
    // List<Achievement> achievements
}
