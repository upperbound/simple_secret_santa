package com.github.upperbound.secret_santa.model;

import com.github.upperbound.secret_santa.model.validation.Email;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.*;

@Accessors(chain = true)
@Getter
@Setter
@Entity
@Table(name = "PARTICIPANT")
@EntityListeners(AuditingEntityListener.class)
public class Participant implements ParticipantDetails {
    @Transient
    public final static Participant ANONYMOUS =
            new Participant()
                    .setUuid("anonymousUser")
                    .setEmail("anonymousUser");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Size(max = 36)
    @Column(name = "UUID", nullable = false, length = 36)
    private String uuid;

    @Size(max = 320)
    @NotNull
    @Email(regexp = "${app.mail.allowed-regex:.*}", message = "${app.mail.allowed-err-message:email '%s' does not match the given mask '%s'}")
    @Column(name = "EMAIL", nullable = false, length = 320)
    private String email;

    @Size(max = 300)
    @NotNull
    @Column(name = "PASSWORD", nullable = false, length = 300)
    private String password;

    @Size(max = 36)
    @Column(name = "SERVICE_ACTION_TOKEN", nullable = false, length = 36)
    private String serviceActionToken;

    @Column(name = "SERVICE_ACTION_TOKEN_DATE")
    private LocalDateTime serviceActionTokenDate;

    @Size(max = 8)
    @NotNull
    @ColumnDefault("'en'")
    @Column(name = "LOCALE", nullable = false, length = 8)
    private String locale;

    @NotNull
    @ColumnDefault("10800000")
    @Column(name = "TIMEZONE_OFFSET", nullable = false)
    private Integer timezoneOffset;

    @Size(max = 36)
    @NotNull
    @ColumnDefault("'Europe/Volgograd'")
    @Column(name = "TIMEZONE_ID", nullable = false, length = 36)
    private String timezoneId;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "IS_SUPERADMIN", nullable = false)
    private Boolean isSuperadmin = false;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "RECEIVE_NOTIFICATIONS", nullable = false)
    private Boolean receiveNotifications = true;

    @Size(max = 2000)
    @NotNull
    @Column(name = "INFO", nullable = false, length = 2000)
    private String info;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "MODIFIED_DATE")
    private LocalDateTime modifiedDate;

    @LastModifiedBy
    @Column(name = "MODIFIED_BY")
    private String modifiedBy;

    @Transient
    private List<ParticipantGroupLink> participantGroupLinks = List.of();

    @Transient
    private ArrayList<GrantedAuthority> authorities;

    @Transient
    private ParticipantRole currentRole;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = new ArrayList<>(participantGroupLinks.size() + 1);
            participantGroupLinks.stream()
                    .filter(link -> link.getRole().getRole() == ParticipantRole.Role.ADMIN)
                    .forEach(link -> authorities.add((GrantedAuthority) () -> link.getGroup().getUuid()));
            if (isSuperadmin)
                authorities.add(ParticipantRole.SUPERADMIN);
        }
        return authorities;
    }

    public boolean isSuperadmin() {
        return isSuperadmin;
    }

    public boolean receiveNotifications() {
        return receiveNotifications;
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
    public List<ParticipantGroupLink> getGroups() {
        return participantGroupLinks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

}