package com.github.upperbound.secret_santa.service;

import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.model.ParticipantRole;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p> Provides any business-related logic with {@link Participant} </p>
 * @author Vladislav Tsukanov
 */
public interface ParticipantService extends UserDetailsService {
    /**
     * Find the participant by UUID
     * @return existed participant or {@code null}
     */
    Participant findById(String participantUuid);

    /**
     * Find all participants
     */
    List<Participant> findAll();

    /**
     * Find the total number of participants within all groups
     */
    Long participantsWithinGroups();

    /**
     * Find all participants by group UUID
     * @throws ServiceException if provided group does not exist
     */
    List<Participant> findAllByGroup(String groupUuid)
            throws ServiceException;

    /**
     * Find the participant by email
     * @return existed participant or {@code null}
     */
    Participant findByEmail(String email);

    /**
     * @see #createIfNotExist(String, String, String, String, String, boolean, TimeZone, Locale) createIfNotExist
     */
    Participant createIfNotExist(String email,
                                 String password,
                                 String info,
                                 boolean receiveNotifications,
                                 TimeZone timeZone,
                                 Locale locale)
            throws ServiceException;

    /**
     * Create the participant if not exists and send a corresponding {@link NotificationEvent notification}.
     * If participant does exist then return existed one
     * @param groupUuid uuid of the group that the participant should be linked to when created
     * @param email the participant's email address
     * @param password the participant's password or {@code null}
     * @param info the participant's info (name, surname, nickname, etc.)
     * @param wishes the participant's wishes
     * @param receiveNotifications does the participant want to receive email notifications or not
     * @param timeZone the participant's timezone
     * @param locale the participant's locale
     * @return created or existed participant
     * @throws ServiceException if the notification has not been delivered to the participant
     */
    Participant createIfNotExist(String groupUuid,
                                 String email,
                                 String password,
                                 String info,
                                 String wishes,
                                 boolean receiveNotifications,
                                 TimeZone timeZone,
                                 Locale locale)
            throws ServiceException;

    /**
     * Set the role to the participant within provided group
     * @param participantEmail the participant's email address
     * @param groupUuid uuid of the group
     * @param role role to set
     * @return {@code true} if the participant is associated with this group and their role has been set,
     * {@code false} otherwise
     * @throws ServiceException if provided group or participant does not exist
     */
    boolean setRole(String participantEmail, String groupUuid, ParticipantRole role)
            throws ServiceException;

    /**
     * Update the participant's wishes information within the group
     * @param participantEmail the participant's email address
     * @param groupUuid uuid of the group
     * @param wishes the participant's wishes
     * @return {@code true} if the participant is associated with this group and their wishes have been updated,
     * {@code false} otherwise
     * @throws ServiceException if provided group or participant does not exist
     */
    boolean updateWishes(String participantEmail, String groupUuid, String wishes)
            throws ServiceException;

    /**
     * Update the participant
     */
    Participant update(Participant participant);

    /**
     * Delete the participant completely from datasource, unlink them from any existed group
     * and send a corresponding {@link NotificationEvent notification}
     * @param participantEmail the participant's email address
     * @param timeZone the participant's timezone
     * @param locale the participant's locale
     * @return {@code true} if the participant does exist and can be deleted (no active groups are linked to them),
     * {@code false} otherwise
     */
    boolean delete(String participantEmail, TimeZone timeZone, Locale locale)
            throws ServiceException;

    /**
     * Get the participant that is authenticated with current session
     */
    Participant getCurrentAuthentication();

    /**
     * Updates the participant that is authenticated with current session
     */
    Participant updateCurrentAuthentication();

    /**
     * <p> Create and send password reset link </p>
     * @param email the participant's email address
     * @param timeZone timezone
     * @param locale locale
     * @throws ServiceException
     */
    void sendResetPasswordLink(String email, TimeZone timeZone, Locale locale) throws ServiceException;

    /**
     * Reset the participant's password
     * @param email the participant's email address
     * @param actionToken previously generated token for a 'password reset' action
     * @param newPassword new password
     * @param timeZone timezone
     * @param locale locale
     * @return {@code true} if password was successfully reset, {@code false} otherwise
     * @throws ServiceException if the participant does not exist or token is wrong or token is expired
     */
    boolean resetPassword(String email,
                          String actionToken,
                          String newPassword,
                          TimeZone timeZone,
                          Locale locale)
            throws ServiceException;
}
