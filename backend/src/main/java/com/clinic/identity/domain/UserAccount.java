package com.clinic.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "app_user")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Column(nullable = false, length = 64)
    private String displayName;

    @Column(nullable = false, length = 32)
    private String roleCode;

    public static UserAccount create(String username, String passwordHash, String displayName, String roleCode) {
        var user = new UserAccount();
        user.username = username;
        user.passwordHash = passwordHash;
        user.displayName = displayName;
        user.roleCode = roleCode;
        return user;
    }
}
