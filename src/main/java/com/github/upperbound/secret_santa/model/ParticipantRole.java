package com.github.upperbound.secret_santa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@Entity
@Table(name = "participant_role")
public class ParticipantRole implements GrantedAuthority {
    @Transient
    public static final ParticipantRole SUPERADMIN = new ParticipantRole();
    @Transient
    public static final ParticipantRole ADMIN = new ParticipantRole();
    @Transient
    public static final ParticipantRole USER = new ParticipantRole();

    static {
        SUPERADMIN.role = Role.SUPERADMIN;
        ADMIN.role = Role.ADMIN;
        USER.role = Role.USER;
    }

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "id", length = 30)
    private Role role;

    @Override
    public String getAuthority() {
        return role.name();
    }

    public enum Role {
        SUPERADMIN,
        ADMIN,
        USER
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantRole that = (ParticipantRole) o;
        return role == that.role;
    }
}
