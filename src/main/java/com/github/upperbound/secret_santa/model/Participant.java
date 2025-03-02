package com.github.upperbound.secret_santa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "participant", uniqueConstraints = {
        @UniqueConstraint(name = "uc_participant_email", columnNames = {"email"})
})
public class Participant implements ParticipantDetails {
    @Transient
    public final static Participant ANONYMOUS = new Participant();

    static {
        ANONYMOUS.setUuid("anonymousUser");
        ANONYMOUS.setEmail("anonymousUser");
    }

    @Id
    @UuidGenerator
    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "group_uuid")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "role")
    private ParticipantRole role = ParticipantRole.USER;

    @OneToOne
    @JoinColumn(name = "participant_to_gift_to")
    private Participant toGiftTo;

    @Email(regexp = ".*")
    @Column(name = "email", nullable = false, unique = true, length = 250)
    private String email;

    @Column(name = "receive_notifications", nullable = false)
    private Boolean receiveNotifications = true;

    @Column(name = "password", nullable = false, length = 36)
    private String password;

    @Column(name = "info", nullable = false, length = 2000)
    private String info;

    @Column(name = "wishes", length = 2000)
    private String wishes;

    @Transient
    private ArrayList<GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = new ArrayList<>(1);
            authorities.add(role);
        }
        return authorities;
    }

    public void setEmail(String email) {
        this.email = email.trim().toLowerCase();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(uuid, that.uuid);
    }
}