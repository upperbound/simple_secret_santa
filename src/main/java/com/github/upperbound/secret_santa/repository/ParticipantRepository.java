package com.github.upperbound.secret_santa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, String> {
    @Query("select p from Participant p where p.email = trim(lower(:email))")
    Optional<Participant> findByEmail(@Param("email") String email);

    @Query(
            "select p from Participant p " +
                    "join ParticipantGroupLink gl on gl.participant = p " +
                    "where gl.group = :group"
    )
    List<Participant> findAllByGroup(@Param("group") Group group);

    @Query(
            "select count(distinct p.uuid) from Participant p " +
                    "join ParticipantGroupLink gl on gl.participant = p "
    )
    Long participantsWithinGroups();

//    @Query(
//            "select p " +
//                    "from Participant p " +
//                    "join ParticipantGroupLink gl on gl.participant = p " +
//                    "where gl.giftee = :giftee"
//    )
//    List<Participant> findAllByGiftee(@Param("giftee") Participant giftee);
}