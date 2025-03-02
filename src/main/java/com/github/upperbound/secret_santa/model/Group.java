package com.github.upperbound.secret_santa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Vladislav Tsukanov
 */
@Accessors(chain = true)
@Getter
@Setter
@Entity
@Table(name = "PARTICIPANT_GROUP")
@EntityListeners(AuditingEntityListener.class)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Size(max = 36)
    @Column(name = "UUID", nullable = false, length = 36)
    private String uuid;

    @Size(max = 100)
    @Column(name = "DESCRIPTION", length = 100)
    private String description;

    @ColumnDefault("false")
    @Column(name = "HAS_DRAWN", nullable = false)
    private boolean hasDrawn = false;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "MODIFIED_DATE")
    private LocalDateTime modifiedDate;

    @LastModifiedBy
    @Column(name = "MODIFIED_BY")
    private String modifiedBy;

    @OneToMany(mappedBy = "group")
    private Set<ParticipantGroupLink> participantGroupLinks = new LinkedHashSet<>();

    public boolean hasDrawn() {
        return hasDrawn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return uuid.equals(group.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

}