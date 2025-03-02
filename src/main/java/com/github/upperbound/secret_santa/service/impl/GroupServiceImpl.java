package com.github.upperbound.secret_santa.service.impl;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.model.*;
import com.github.upperbound.secret_santa.repository.ParticipantGroupLinkRepository;
import com.github.upperbound.secret_santa.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.upperbound.secret_santa.repository.GroupRepository;
import com.github.upperbound.secret_santa.repository.ParticipantRepository;
import com.github.upperbound.secret_santa.util.StaticContext;

import java.util.*;

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GroupServiceImpl implements GroupService {
    private static final String EXCEPTION_MESSAGE_TEMPLATE = "unable to perform group action: {}";
    private final StaticContext staticContext;
    private final NotificationEventFactory notificationEventFactory;
    private final GroupRepository groupRepository;
    private final ParticipantGroupLinkRepository groupLinkRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantService participantService;

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Group update(Group group) {
        return groupRepository.save(group);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean delete(String groupUuid) {
        Group g = groupRepository.findById(groupUuid).orElse(null);
        if (g == null)
            return false;
        groupLinkRepository.deleteAllByGroup(g);
        groupRepository.delete(g);
        return true;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean linkParticipant(String groupUuid, String participantEmail, TimeZone timeZone, Locale locale)
            throws ServiceException
    {
        return linkParticipant(groupUuid, participantEmail, ParticipantRole.USER, timeZone, locale);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean linkParticipant(String groupUuid,
                                   String participantEmail,
                                   ParticipantRole role,
                                   TimeZone timeZone,
                                   Locale locale)
            throws ServiceException
    {
        return linkParticipant(groupUuid, participantEmail, role, null, timeZone, locale);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean linkParticipant(String groupUuid,
                                   String participantEmail,
                                   String wishes,
                                   TimeZone timeZone,
                                   Locale locale)
            throws ServiceException
    {
        return linkParticipant(groupUuid, participantEmail, ParticipantRole.USER, wishes, timeZone, locale);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean linkParticipant(String groupUuid,
                                   String participantEmail,
                                   ParticipantRole role,
                                   String wishes,
                                   TimeZone timeZone,
                                   Locale locale)
            throws ServiceException
    {
        Participant participant  = participantRepository.findByEmail(participantEmail).orElseThrow(
                () -> new ServiceException(ExceptionCode.PARTICIPANT_NOT_EXIST, participantEmail)
        );
        ParticipantGroupLinkId groupLinkId = new ParticipantGroupLinkId()
                .setGroupUuid(groupUuid)
                .setParticipantUuid(participant.getUuid());
        ParticipantGroupLink groupLink = groupLinkRepository.findById(groupLinkId).orElse(new ParticipantGroupLink());
        if (groupLink.getId() != null)
            return true;
        Group g = groupRepository.findById(groupUuid).orElseThrow(
                () -> new ServiceException(ExceptionCode.GROUP_NOT_EXIST, groupUuid)
        );
        groupLink.setId(groupLinkId)
                .setParticipant(participant)
                .setGroup(g)
                .setRole(role)
                .setWishes(wishes);
        if (!g.hasDrawn()) {
            groupLinkRepository.save(groupLink);
            return true;
        }
        ParticipantGroupLink forReplace = null;
        List<ParticipantGroupLink> targetGroupParticipants = groupLinkRepository.findAllByGroup(g);
        targetGroupParticipants.stream()
                .filter(link -> link.getParticipant().equals(participantService.getCurrentAuthentication()))
                .findFirst()
                .ifPresent(targetGroupParticipants::remove);
        if (!targetGroupParticipants.isEmpty()) {
            Random random = new Random();
            forReplace = targetGroupParticipants.get(random.nextInt(0, targetGroupParticipants.size()));
            groupLink.setGiftee(forReplace.getGiftee());
            forReplace.setGiftee(participant);
            groupLinkRepository.save(forReplace);
        }
        groupLinkRepository.save(groupLink);
        groupLinkRepository.flush();
        if (forReplace != null) {
            notificationEventFactory.fireNotification(
                    participant.getEmail(),
                    staticContext.getMailCommonSubject(g.getDescription(), locale),
                    staticContext.getMailDrawChangesText(
                            groupLink.getGiftee().getEmail(),
                            groupLink.getGiftee().getInfo(),
                            groupLinkRepository.findById(
                                    new ParticipantGroupLinkId()
                                            .setGroupUuid(groupUuid)
                                            .setParticipantUuid(groupLink.getGiftee().getUuid())
                            ).orElse(new ParticipantGroupLink()).getWishes(),
                            null,
                            timeZone,
                            locale
                    )
            );
            notificationEventFactory.fireNotification(
                    forReplace.getParticipant().getEmail(),
                    staticContext.getMailCommonSubject(g.getDescription(), locale),
                    staticContext.getMailDrawChangesText(
                            forReplace.getGiftee().getEmail(),
                            forReplace.getGiftee().getInfo(),
                            groupLink.getWishes(),
                            null,
                            timeZone,
                            locale
                    )
            );
        }
        return true;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean unlinkParticipant(String groupUuid, String participantEmail, TimeZone timeZone, Locale locale)
            throws ServiceException
    {
        Participant participant = participantRepository.findByEmail(participantEmail).orElse(new Participant());
        ParticipantGroupLinkId groupLinkId = new ParticipantGroupLinkId()
                .setGroupUuid(groupUuid)
                .setParticipantUuid(participant.getUuid());
        ParticipantGroupLink groupLink = groupLinkRepository.findById(groupLinkId).orElse(null);
        if (groupLink == null)
            return true;
        Participant giftee = groupLink.getGiftee();
        if (!groupLink.getGroup().hasDrawn() || giftee == null) {
            groupLinkRepository.delete(groupLink);
            return true;
        }
        ParticipantGroupLink gifteeLink = groupLinkRepository.findById(
                new ParticipantGroupLinkId()
                        .setGroupUuid(groupUuid)
                        .setParticipantUuid(giftee.getUuid())
        ).orElse(new ParticipantGroupLink());
        List<ParticipantGroupLink> santas = groupLinkRepository.findAllByGiftee(participant);
        for (ParticipantGroupLink santa : santas) {
            santa.setGiftee(giftee);
            santa.setGifteeGroupLink(null);
            groupLinkRepository.save(santa);
            log.info("for participant '{}' added new giftee '{}'", santa.getParticipant().getUuid(), giftee.getUuid());
            notificationEventFactory.fireNotification(
                    santa.getParticipant().getEmail(),
                    staticContext.getMailCommonSubject(santa.getGroup().getDescription(), locale),
                    staticContext.getMailDrawChangesText(
                            giftee.getEmail(),
                            giftee.getInfo(),
                            gifteeLink.getWishes(),
                            null,
                            timeZone,
                            locale
                    )
            );
        }
        groupLinkRepository.delete(groupLink);
        return true;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Group findById(String uuid) {
        return groupRepository.findById(uuid).orElse(null);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Group findByDescription(String description) {
        return groupRepository.findByDescription(description).orElse(null);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Optional<ParticipantGroupLink> findLink(String participantUuid, String groupUuid) {
        return groupRepository.findLink(participantUuid, groupUuid);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public List<ParticipantGroupLink> findLinksByParticipant(Participant participant) {
        return groupLinkRepository.findAllByParticipant(participant);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public List<Group> findAllByHasDrawnFalse() {
        return groupRepository.findAllByHasDrawnFalse();
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public List<Group> findAllByParticipant(Participant participant) {
        return groupRepository.findAllByParticipant(participant);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean updateWishes(Group group, Participant participant, String wishes) {
        ParticipantGroupLink groupLink = groupRepository.findLink(participant.getUuid(), group.getUuid())
                .orElse(null);
        if (groupLink == null)
            return false;
        groupLink.setWishes(wishes);
        groupLinkRepository.save(groupLink);
        return true;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Group create(String description, Participant participant) {
        Group group = groupRepository.findByDescription(description).orElse(new Group());
        if (group.getUuid() != null)
            return group;
        log.info("creating new group '{}'", description);
        group = groupRepository.save(group.setDescription(description));
        groupLinkRepository.save(
                new ParticipantGroupLink()
                        .setId(new ParticipantGroupLinkId().setGroupUuid(group.getUuid()).setParticipantUuid(participant.getUuid()))
                        .setParticipant(participant)
                        .setGroup(group)
                        .setRole(ParticipantRole.ADMIN)
        );
        return group;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean resetDraw(String groupUuid, TimeZone timeZone, Locale locale) throws ServiceException {
        Group g = groupRepository.findById(groupUuid).orElseThrow(
                () -> new ServiceException(ExceptionCode.GROUP_NOT_EXIST, groupUuid)
        );
        if (!g.hasDrawn())
            return false;
        g.setHasDrawn(false);
        groupRepository.save(g);
        List<ParticipantGroupLink> groupLinks = groupLinkRepository.findAllByGroup(g);
        if (groupLinks.isEmpty())
            return false;
        log.info("reset draw results on '{}'", g.getDescription());
        groupLinks.forEach(groupLink -> {
            groupLink.setGiftee(null);
            groupLinkRepository.save(groupLink);
        });
        groupRepository.flush();
        groupLinkRepository.flush();
        for (ParticipantGroupLink groupLink : groupLinks) {
            if (groupLink.getParticipant().getReceiveNotifications())
                notificationEventFactory.fireNotification(
                        groupLink.getParticipant().getEmail(),
                        staticContext.getMailCommonSubject(g.getDescription(), locale),
                        staticContext.getMailDrawCancelText(timeZone, locale)
                );
        }
        return true;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean makeADraw(String groupUuid, TimeZone timeZone, Locale locale) throws ServiceException {
        Group g = groupRepository.findById(groupUuid).orElseThrow(
                () -> new ServiceException(ExceptionCode.GROUP_NOT_EXIST, groupUuid)
        );
        if (g.hasDrawn())
            return false;
        List<ParticipantGroupLink> groupLinks = groupLinkRepository.findAllByGroup(g);
        if (groupLinks.isEmpty())
            return false;
        log.info("making a draw for '{}'", g.getDescription());
        List<ParticipantGroupLink> result = new ArrayList<>(groupLinks.size());
        Map<Participant, String> map = new HashMap<>(groupLinks.size());
        Random random = new Random();
        ParticipantGroupLink first = groupLinks.get(random.nextInt(0, groupLinks.size()));
        result.add(first);
        groupLinks.remove(first);
        ParticipantGroupLink current = first;
        while (!groupLinks.isEmpty()) {
            ParticipantGroupLink giftee = groupLinks.get(random.nextInt(0, groupLinks.size()));
            current.setGiftee(giftee.getParticipant());
            map.put(current.getParticipant(), current.getWishes());
            log.trace("giftee '{}' assigned to participant '{}'", giftee.getParticipant().getUuid(), current.getParticipant().getUuid());
            groupLinkRepository.save(current);
            groupLinks.remove(giftee);
            current = giftee;
            result.add(current);
        }
        current.setGiftee(first.getParticipant());
        map.put(current.getParticipant(), current.getWishes());
        log.trace("giftee '{}' assigned to participant '{}'", first.getParticipant().getUuid(), current.getParticipant().getUuid());
        g.setHasDrawn(true);
        groupLinkRepository.save(current);
        groupRepository.save(g);
        groupRepository.flush();
        groupLinkRepository.flush();
        for (ParticipantGroupLink groupLink : result) {
            if (groupLink.getParticipant().getReceiveNotifications())
                notificationEventFactory.fireNotification(
                        groupLink.getParticipant().getEmail(),
                        staticContext.getMailCommonSubject(g.getDescription(), locale),
                        staticContext.getMailDrawResultsText(
                                groupLink.getGiftee().getEmail(),
                                groupLink.getGiftee().getInfo(),
                                map.get(groupLink.getGiftee()),
                                null,
                                timeZone,
                                locale
                        )
                );
        }
        return true;
    }
}
