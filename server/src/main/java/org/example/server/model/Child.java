package org.example.server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "child", indexes = {
    @Index(name = "idx_child_parent_id", columnList = "parent_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_child_parent_name", columnNames = { "parent_id", "name" })
})
public class Child {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", nullable = false)
  private Parent parent;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  @Min(7)
  @Max(24)
  private int age;

  @Column(nullable = true)
  private String pinHash; // Pattern/PIN acts as the child's password (nullable for first-time setup)

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
