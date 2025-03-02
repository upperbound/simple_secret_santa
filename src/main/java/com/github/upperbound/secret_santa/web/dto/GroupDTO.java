package com.github.upperbound.secret_santa.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

@Data
@Accessors(chain = true)
@Schema(description = "Group information")
public class GroupDTO {
    @NotBlank
    @Schema(description = "UUID")
    private String uuid;
    @NotBlank
    @Schema(description = "Description")
    private String description;
    @NotNull
    @Schema(description = "Has already been drawn")
    private Boolean hasDrawn;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        GroupDTO other = (GroupDTO) object;
        return Objects.equals(getUuid(), other.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }
}
