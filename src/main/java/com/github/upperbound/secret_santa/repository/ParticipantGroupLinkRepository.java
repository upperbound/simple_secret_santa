package com.github.upperbound.secret_santa.repository;

import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.model.ParticipantGroupLink;
import com.github.upperbound.secret_santa.model.ParticipantGroupLinkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantGroupLinkRepository extends JpaRepository<ParticipantGroupLink, ParticipantGroupLinkId> {
    List<ParticipantGroupLink> findAllByParticipant(Participant participant);
    List<ParticipantGroupLink> findAllByGroup(Group group);
    @Query(
            "select gl from ParticipantGroupLink gl where gl.giftee = :giftee"
    )
    List<ParticipantGroupLink> findAllByGiftee(Participant giftee);
    @Modifying
    @Query(
            "delete from ParticipantGroupLink gl where gl.group = :group"
    )
    void deleteAllByGroup(Group group);
}