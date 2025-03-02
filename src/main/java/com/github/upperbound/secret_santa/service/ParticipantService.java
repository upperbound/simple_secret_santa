package com.github.upperbound.secret_santa.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.model.ParticipantRole;

import java.util.List;

public interface ParticipantService extends UserDetailsService {
    void sendCredentialsToUser(String email) throws ServiceException;
    Participant findById(String uuid);
    List<Participant> findAll();
    List<Participant> findAllByGroup(Group group);
    Participant findByEmail(String email);
    boolean updateRole(String email, ParticipantRole role);
    boolean swapRole(String uuid);
    Participant createIfNotExists(String groupUuid,
                                  String email,
                                  String password,
                                  String info,
                                  String wishes,
                                  boolean receiveNotifications) throws ServiceException;
    Participant createIfNotExists(Group group,
                                  String email,
                                  String password,
                                  String info,
                                  String wishes,
                                  boolean receiveNotifications) throws ServiceException;
    Participant update(Participant participant);
    boolean moveParticipant(String uuid, Group group) throws ServiceException;
    boolean delete(String uuid) throws ServiceException;
    Participant getCurrentAuthentication();
    boolean sendDeleteInfoToUser(Participant participant) throws ServiceException;
}
