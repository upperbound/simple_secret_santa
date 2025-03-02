package com.github.upperbound.secret_santa.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

@Data
@Accessors(chain = true)
@Schema(description = "Link between group and participant")
public class ParticipantGroupDTO {
    @NotBlank
    @Schema(description = "Group UUID")
    private String groupUuid;
    @NotBlank
    @Schema(description = "Participant UUID")
    private String participantUuid;
    @NotBlank
    @Schema(description = "Participant's role in this group", defaultValue = "USER")
    private String role;
    @Schema(description = "Participant's wishes for a gift in this group")
    private String wishes;
    @Schema(description = "A person to whom this participant should present a gift")
    private ParticipantDTO giftee;
    @Schema(description = "Giftee's wishes")
    private String gifteeWishes;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ParticipantGroupDTO other = (ParticipantGroupDTO) object;
        return Objects.equals(getGroupUuid(), other.getGroupUuid())
                && Objects.equals(getParticipantUuid(), other.getParticipantUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupUuid(), getParticipantUuid());
    }
}
