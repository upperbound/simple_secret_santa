package com.github.upperbound.secret_santa.service;

import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.model.ParticipantGroupLink;
import com.github.upperbound.secret_santa.model.ParticipantRole;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

/**
 * <p> Provides any business-related logic with {@link Group} </p>
 * @author Vladislav Tsukanov
 */
public interface GroupService {
    /**
     * Update the group
     */
    Group update(Group group);

    /**
     * Delete the group
     * @return if the group does exist and deleted, {@code false} otherwise
     */
    boolean delete(String groupUuid);

    /**
     * @see #linkParticipant(String, String, ParticipantRole, String, TimeZone, Locale) linkParticipant
     */
    boolean linkParticipant(String groupUuid, String participantEmail, TimeZone timeZone, Locale locale)
            throws ServiceException;

    /**
     * @see #linkParticipant(String, String, ParticipantRole, String, TimeZone, Locale) linkParticipant
     */
    boolean linkParticipant(String groupUuid,
                            String participantEmail,
                            ParticipantRole role,
                            TimeZone timeZone,
                            Locale locale)
            throws ServiceException;

    /**
     * @see #linkParticipant(String, String, ParticipantRole, String, TimeZone, Locale) linkParticipant
     */
    boolean linkParticipant(String groupUuid,
                            String participantEmail,
                            String wishes,
                            TimeZone timeZone,
                            Locale locale)
            throws ServiceException;

    /**
     * <p> Link the participant to the group. If the results within this group have been already drawn then
     * it finds a random participant, sets the current one as a giftee for the found one and for the current one
     * sets a giftee that have been associated with the found participant. </p>
     * <p> Also sends corresponding {@link NotificationEvent notifications} in all aforementioned cases </p>
     * @param groupUuid uuid of the group
     * @param participantEmail the participant's email address
     * @param role the role to set within the group
     * @param wishes the participant's wishes
     * @param timeZone timezone
     * @param locale locale
     * @return {@code true} if the participant has been associated with this group,
     * {@code false} if the participant already associated with this group
     * @throws ServiceException if provided group or participant does not exist
     */
    boolean linkParticipant(String groupUuid,
                            String participantEmail,
                            ParticipantRole role,
                            String wishes,
                            TimeZone timeZone,
                            Locale locale)
            throws ServiceException;

    /**
     * <p> Unlink the participant from the group. If the results within this group have been already drawn then
     * it finds the participant for whom the current one was as a giftee and sets a new giftee from the current one. </p>
     * <p> Also sends corresponding {@link NotificationEvent notifications} in all aforementioned cases </p>
     * @param groupUuid uuid of the group
     * @param participantEmail the participant's email address
     * @param timeZone timezone
     * @param locale locale
     * @return {@code true} if the participant was unlinked from this group,
     * {@code false} if the participant was not associated with this group or if the results have been drawn
     */
    boolean unlinkParticipant(String groupUuid, String participantEmail, TimeZone timeZone, Locale locale)
            throws ServiceException;

    /**
     * Find group by UUID
     * @return existed group or {@code null}
     */
    Group findById(String uuid);

    /**
     * Find group by description
     * @return existed group or {@code null}
     */
    Group findByDescription(String description);

    /**
     * Find group link by participant and group
     * @return existed link or {@code null}
     */
    Optional<ParticipantGroupLink> findLink(String participantUuid, String groupUuid);

    /**
     * Find all group links by participant
     */
    List<ParticipantGroupLink> findLinksByParticipant(Participant participant);

    /**
     * Find all groups
     */
    List<Group> findAll();

    /**
     * Find all groups where the results have not been drawn yet
     */
    List<Group> findAllByHasDrawnFalse();

    /**
     * Find participant's groups or all groups if the participant is superadmin
     */
    List<Group> findAllByParticipant(Participant participant);

    boolean updateWishes(Group group, Participant participant, String wishes);

    /**
     * Create a new group if it does not exist yet
     * @param description custom name for a group
     */
    Group create(String description, Participant participant);

    /**
     * Reset all the results and send corresponding {@link NotificationEvent notifications}
     * @param groupUuid uuid of the group
     * @param timeZone timezone
     * @param locale locale
     * @return {@code true} if the results have been reset,
     * {@code false} if the results have already been reset or if there are no participants related to this group
     * @throws ServiceException if the group does not exist
     */
    boolean resetDraw(String groupUuid, TimeZone timeZone, Locale locale)
            throws ServiceException;

    /**
     * Make a draw for the participants of this group and send corresponding {@link NotificationEvent notifications}
     * @param groupUuid uuid of the group
     * @param timeZone timezone
     * @param locale locale
     * @return {@code true} if the results have been drawn,
     * {@code false} if the results have already been drawn or if there are no participants related to this group
     * @throws ServiceException if the group does not exist
     */
    boolean makeADraw(String groupUuid, TimeZone timeZone, Locale locale)
            throws ServiceException;
}
