package com.github.upperbound.secret_santa.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Schema(description = "Participant information")
public class ParticipantDTO {
    @NotBlank
    @Schema(description = "UUID")
    private String uuid;
    @NotBlank
    @Schema(description = "email")
    private String email;
    @NotBlank
    @Schema(description = "Any additional information: Name, Surname, Nickname, etc.")
    private String info;
    @Schema(description = "Preferred locale")
    private String locale;
    @Schema(description = "Timezone offset in milliseconds")
    private Integer timezoneOffset;
    @Schema(description = "Timezone ID")
    private String timezoneId;
    @Schema(description = "Does this participant want receive notifications")
    private Boolean receiveNotifications;
    @Schema(description = "List of groups")
    private List<ParticipantGroupDTO> participantGroups;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ParticipantDTO other = (ParticipantDTO) object;
        return Objects.equals(getUuid(), other.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }
}
