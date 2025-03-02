package com.github.upperbound.secret_santa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Accessors(chain = true)
@Getter
@Setter
@Embeddable
public class ParticipantGroupLinkId implements Serializable {
    @Serial
    private static final long serialVersionUID = 4634673165672458660L;

    @Size(max = 36)
    @NotNull
    @Column(name = "GROUP_UUID", nullable = false, length = 36)
    private String groupUuid;

    @Size(max = 36)
    @NotNull
    @Column(name = "PARTICIPANT_UUID", nullable = false, length = 36)
    private String participantUuid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ParticipantGroupLinkId entity = (ParticipantGroupLinkId) o;
        return this.groupUuid.equals(entity.groupUuid) &&
                this.participantUuid.equals(entity.participantUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupUuid, participantUuid);
    }

}