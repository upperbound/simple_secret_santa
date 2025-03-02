package com.github.upperbound.secret_santa.model;

import org.springframework.security.core.userdetails.UserDetails;

public interface ParticipantDetails extends UserDetails {
    String getUuid();
    Group getGroup();
}
