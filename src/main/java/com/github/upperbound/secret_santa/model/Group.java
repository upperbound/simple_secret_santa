package com.github.upperbound.secret_santa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "participant_group", uniqueConstraints = {
        @UniqueConstraint(name = "uc_group_description", columnNames = {"description"})
})
public class Group {
    @Id
    @UuidGenerator
    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Column(name = "has_drawn")
    private Boolean hasDrawn = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(uuid, group.uuid);
    }
}