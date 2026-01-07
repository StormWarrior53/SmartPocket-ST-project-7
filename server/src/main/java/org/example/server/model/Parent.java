package org.example.server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Parent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false)
  @Email
  @NotBlank
  private String email;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = true) // Nullable for OAuth users who don't have a password
  private String passwordHash;

  @Column(name = "google_id", unique = true)
  private String googleId; // Google's unique user ID (sub claim from OAuth)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private AuthProvider authProvider = AuthProvider.LOCAL; // Authentication provider (LOCAL or GOOGLE)

  @Column(nullable = false)
  @Builder.Default
  private boolean isAdmin = false;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Child> children = new ArrayList<>();

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;

  public void addChild(Child child) {
    children.add(child);
    child.setParent(this);
  }

  public void removeChild(Child child) {
    children.remove(child);
    child.setParent(null);
  }

  /**
   * Helper method to check if this parent account uses OAuth authentication.
   * 
   * @return true if this is an OAuth account, false for local email/password
   */
  public boolean isOAuthAccount() {
    return authProvider == AuthProvider.GOOGLE;
  }
}
