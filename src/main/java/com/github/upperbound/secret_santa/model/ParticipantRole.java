package com.github.upperbound.secret_santa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;

@Accessors(chain = true)
@Getter
@Setter
@Entity
@Table(name = "PARTICIPANT_ROLE")
public class ParticipantRole implements GrantedAuthority {
    public enum Role {
        SUPERADMIN,
        ADMIN,
        USER
    }
    @Transient
    public static final ParticipantRole SUPERADMIN = new ParticipantRole().setRole(Role.SUPERADMIN);
    @Transient
    public static final ParticipantRole ADMIN = new ParticipantRole().setRole(Role.ADMIN);
    @Transient
    public static final ParticipantRole USER = new ParticipantRole().setRole(Role.USER);

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "ID", length = 30)
    private Role role;

    @Override
    public String getAuthority() {
        return role.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantRole that = (ParticipantRole) o;
        return role == that.role;
    }

    @Override
    public int hashCode() {
        return role.hashCode();
    }
}
