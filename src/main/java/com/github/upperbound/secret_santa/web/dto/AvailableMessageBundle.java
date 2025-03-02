package com.github.upperbound.secret_santa.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Schema(description = "Available i18n bundle")
public class AvailableMessageBundle {
    @NotBlank
    @Schema(description = "IETF language tag", example = "en")
    private String lang;
    @NotBlank
    @Schema(description = "Relative path to the image for this language", example = "/images/en.png")
    private String imageUrl;
    @JsonIgnore
    private Locale locale;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AvailableMessageBundle other = (AvailableMessageBundle) object;
        return Objects.equals(getLocale(), other.getLocale());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getLocale());
    }

    public Locale getLocale() {
        if (locale == null && lang != null)
            locale = StringUtils.parseLocale(lang);
        return locale;
    }
}
