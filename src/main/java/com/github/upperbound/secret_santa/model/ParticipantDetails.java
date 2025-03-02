package com.github.upperbound.secret_santa.model;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * <p> Provides additional data for {@link Participant} </p>
 * @author Vladislav Tsukanov
 */
public interface ParticipantDetails extends UserDetails {
    String getUuid();
    List<ParticipantGroupLink> getGroups();
}
