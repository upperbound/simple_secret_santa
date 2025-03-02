package com.github.upperbound.secret_santa.web.dto;

import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.model.ParticipantGroupLink;
import com.github.upperbound.secret_santa.model.ParticipantRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DTOMapper {
    @Mapping(target = "participantGroups", source = "participantGroupLinks")
    ParticipantDTO toParticipantDTO(Participant participant);

    @Mapping(target = "locale", ignore = true)
    @Mapping(target = "timezoneId", ignore = true)
    @Mapping(target = "timezoneOffset", ignore = true)
    @Mapping(target = "receiveNotifications", ignore = true)
    @Mapping(target = "participantGroups", ignore = true)
    @Named("participantBareMinimum")
    ParticipantDTO toParticipantDTOBareMinimum(Participant participant);

    GroupDTO toGroupDTO(Group group);

    @Mapping(target = "role", source = "role.role")
    @Mapping(target = "giftee", source = "giftee", qualifiedByName = "participantBareMinimum")
    @Mapping(target = "gifteeWishes", source = "gifteeGroupLink.wishes")
    ParticipantGroupDTO toParticipantGroupDTO(ParticipantGroupLink participantGroup);

    ParticipantRole toParticipantRole(ParticipantRole.Role role);

    ParticipantRole.Role toRole(String value);

    String toStringRole(ParticipantRole.Role role);
}
