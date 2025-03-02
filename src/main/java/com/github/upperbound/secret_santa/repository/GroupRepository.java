package com.github.upperbound.secret_santa.repository;

import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.model.ParticipantGroupLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.github.upperbound.secret_santa.model.Group;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    Optional<Group> findByDescription(String description);
    List<Group> findAllByHasDrawnFalse();
    @Query(
            "select gl from ParticipantGroupLink gl " +
                    "where gl.participant.uuid = :participantUuid and gl.group.uuid = :groupUuid"
    )
    Optional<ParticipantGroupLink> findLink(@Param("participantUuid") String participantUuid, @Param("groupUuid") String groupUuid);
    @Query(
            "select distinct g from Group g " +
                    "left join ParticipantGroupLink gl on gl.group = g and gl.participant = :participant " +
                    "where :#{#participant.isSuperadmin} = true or gl is not null"
    )
    List<Group> findAllByParticipant(@Param("participant") Participant participant);
}