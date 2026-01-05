package com.rafhi.entity;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.Username;
import io.quarkus.security.jpa.UserDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
@UserDefinition
public class AppUser extends PanacheEntity {

    /**
     * Username untuk login.
     * Digunakan oleh Quarkus Security (JPA Identity Provider).
     */
    @Username
    @Column(nullable = false, unique = true)
    public String username;

    /**
     * Password yang sudah di-hash menggunakan bcrypt.
     * Akan diverifikasi otomatis oleh Quarkus saat Basic Auth.
     */
    @Password
    @Column(nullable = false)
    public String password;

    /**
     * Role user (misalnya: ADMIN, USER).
     * Digunakan untuk authorization.
     */
    @Roles
    @Column(nullable = false)
    public String role;

    /**
     * Helper method untuk registrasi user baru.
     * Password di-hash sebelum disimpan ke database.
     */
    public static void add(String username, String rawPassword, String role) {
        AppUser user = new AppUser();
        user.username = username;
        user.password = BcryptUtil.bcryptHash(rawPassword); 
        user.role = role;
        user.persist();
    }
}
