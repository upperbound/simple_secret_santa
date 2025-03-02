package com.github.upperbound.secret_santa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

@Accessors(chain = true)
@Getter
@Setter
@Entity
@Table(name = "PARTICIPANT_GROUP_LINK")
@EntityListeners(AuditingEntityListener.class)
public class ParticipantGroupLink {
    @EmbeddedId
    private ParticipantGroupLinkId id;

    @MapsId("groupUuid")
    @ManyToOne(optional = false)
    @JoinColumn(name = "GROUP_UUID", referencedColumnName = "UUID", nullable = false)
    private Group group;

    @MapsId("participantUuid")
    @ManyToOne(optional = false)
    @JoinColumn(name = "PARTICIPANT_UUID", referencedColumnName = "UUID", nullable = false)
    private Participant participant;

    @NotNull
    @ColumnDefault("'USER'")
    @ManyToOne
    @JoinColumn(name = "ROLE", referencedColumnName = "ID")
    private ParticipantRole role = ParticipantRole.USER;

    @ManyToOne
    @JoinColumn(name = "GIFTEE_UUID", referencedColumnName = "UUID")
    private Participant giftee;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "GIFTEE_UUID", referencedColumnName = "PARTICIPANT_UUID", insertable = false, updatable = false),
            @JoinColumn(name = "GROUP_UUID", referencedColumnName = "GROUP_UUID", insertable = false, updatable = false)
    })
    private ParticipantGroupLink gifteeGroupLink;

    @Size(max = 4000)
    @Column(name = "WISHES", length = 4000)
    private String wishes;

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

}