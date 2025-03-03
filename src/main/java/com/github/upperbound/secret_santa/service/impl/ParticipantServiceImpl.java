package com.github.upperbound.secret_santa.service.impl;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.model.*;
import com.github.upperbound.secret_santa.repository.GroupRepository;
import com.github.upperbound.secret_santa.repository.ParticipantGroupLinkRepository;
import com.github.upperbound.secret_santa.repository.ParticipantRepository;
import com.github.upperbound.secret_santa.service.*;
import com.github.upperbound.secret_santa.util.StaticContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipantServiceImpl implements ParticipantService {
    private static final String EXCEPTION_MESSAGE_TEMPLATE = "unable to perform participant action: {}";
    private final StaticContext staticContext;
    private final PasswordFactory passwordFactory;
    private final NotificationEventFactory notificationEventFactory;
    private final GroupRepository groupRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantGroupLinkRepository groupLinkRepository;

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Participant findById(String participantUuid) {
        Participant result = participantRepository.findById(participantUuid).orElse(null);
        if (result != null) {
            result.setParticipantGroupLinks(groupLinkRepository.findAllByParticipant(result));
        }
        return result;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public List<Participant> findAll() {
        return participantRepository.findAll();
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Long participantsWithinGroups() {
        return participantRepository.participantsWithinGroups();
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Participant findByEmail(String email) {
        Participant result = participantRepository.findByEmail(email).orElse(null);
        if (result != null) {
            result.setParticipantGroupLinks(groupLinkRepository.findAllByParticipant(result));
        }
        return result;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Participant createIfNotExist(String email,
                                        String password,
                                        String info,
                                        boolean receiveNotifications,
                                        TimeZone timeZone,
                                        Locale locale)
            throws ServiceException
    {
        return createIfNotExist(null, email, password, info, null, receiveNotifications, timeZone, locale);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Participant createIfNotExist(String groupUuid,
                                        String email,
                                        String password,
                                        String info,
                                        String wishes,
                                        boolean receiveNotifications,
                                        TimeZone timeZone,
                                        Locale locale)
            throws ServiceException
    {
        Group g = null;
        if (groupUuid != null)
            g = groupRepository.findById(groupUuid).orElse(null);
        Participant participant = participantRepository.findByEmail(email).orElse(new Participant());
        if (participant.getUuid() != null) {
            if (g != null) {
                addParticipantGroupLink(participant, g, wishes);
            }
            return participant;
        }
        log.info("creating a new user: [{}, {}]", email, info);
        participant.setEmail(email)
                .setPassword("stub")
                .setInfo(info)
                .setReceiveNotifications(receiveNotifications)
                .setLocale(locale.toString())
                .setTimezoneId(timeZone.getID())
                .setTimezoneOffset(timeZone.getRawOffset());
        participantRepository.save(participant);
        if (g != null) {
            addParticipantGroupLink(participant, g, wishes);
        }
        participantRepository.flush();
        groupLinkRepository.flush();
        NotificationEvent notificationEvent = notificationEventFactory.fireNotification(
                email,
                staticContext.getMailCommonSubject(locale),
                staticContext.getMailParticipantRegistrationText(timeZone, locale),
                0
        );
        if (NotificationEvent.Status.FAILED == notificationEvent.waitForSend()) {
            if (notificationEvent.getLastException() != null)
                throw notificationEvent.getLastException();
            else
                throw new ServiceException(ExceptionCode.EMAIL_DELIVERY_EXCEPTION, email);
        }
        // if notification has been successfully delivered above then we will create an actual password
        return participantRepository.save(participant.setPassword(passwordFactory.createPassword(password, email, locale)));
    }

    private void addParticipantGroupLink(Participant participant, Group group, String wishes) {
        ParticipantGroupLinkId groupLinkId = new ParticipantGroupLinkId()
                .setGroupUuid(group.getUuid())
                .setParticipantUuid(participant.getUuid());
        ParticipantGroupLink link = groupLinkRepository.findById(groupLinkId)
                .orElse(new ParticipantGroupLink().setId(groupLinkId).setGroup(group).setParticipant(participant));
        if (wishes != null && !wishes.isBlank())
            link.setWishes(wishes);
        groupLinkRepository.save(link);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Participant update(Participant participant) {
        return participantRepository.save(participant);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    public boolean delete(String participantEmail, TimeZone timeZone, Locale locale) throws ServiceException {
        Participant participant = findByEmail(participantEmail);
        if (participant == null)
            return false;
        boolean canBeDeleted = true;
        for (ParticipantGroupLink groupLink : participant.getParticipantGroupLinks()) {
            canBeDeleted &= !groupLink.getGroup().hasDrawn();
        }
        if (canBeDeleted) {
            log.info("deleting participant '{}'", participant.getUuid());
            groupLinkRepository.deleteAll(participant.getParticipantGroupLinks());
            participantRepository.delete(participant);
            notificationEventFactory.fireNotification(
                    participant.getEmail(),
                    staticContext.getMailCommonSubject(locale),
                    staticContext.getMailParticipantDeletedText(false, timeZone, locale)
            );
            return true;
        }
        return false;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public List<Participant> findAllByGroup(String groupUuid)
            throws ServiceException
    {
        Group g = groupRepository.findById(groupUuid).orElseThrow(
                () -> new ServiceException(ExceptionCode.GROUP_NOT_EXIST, groupUuid)
        );
        List<Participant> result = participantRepository.findAllByGroup(g);
        result.forEach(participant -> participant.getParticipantGroupLinks().size());
        return result;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean setRole(String participantEmail, String groupUuid, ParticipantRole role)
            throws ServiceException
    {
        Participant participant  = participantRepository.findByEmail(participantEmail).orElseThrow(
                () -> new ServiceException(ExceptionCode.PARTICIPANT_NOT_EXIST, participantEmail)
        );
        Group g = groupRepository.findById(groupUuid).orElseThrow(
                () -> new ServiceException(ExceptionCode.GROUP_NOT_EXIST, groupUuid)
        );
        ParticipantGroupLink groupLink = groupLinkRepository.findById(
                new ParticipantGroupLinkId()
                        .setGroupUuid(g.getUuid())
                        .setParticipantUuid(participant.getUuid())
        ).orElse(null);
        if (groupLink == null)
            return false;
        groupLinkRepository.save(groupLink.setRole(role));
        return true;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean updateWishes(String participantEmail, String groupUuid, String wishes)
            throws ServiceException
    {
        Participant participant  = participantRepository.findByEmail(participantEmail).orElseThrow(
                () -> new ServiceException(ExceptionCode.PARTICIPANT_NOT_EXIST, participantEmail)
        );
        Group g = groupRepository.findById(groupUuid).orElseThrow(
                () -> new ServiceException(ExceptionCode.GROUP_NOT_EXIST, groupUuid)
        );
        ParticipantGroupLink groupLink = groupLinkRepository.findById(
                new ParticipantGroupLinkId()
                        .setGroupUuid(g.getUuid())
                        .setParticipantUuid(participant.getUuid())
        ).orElse(null);
        if (groupLink == null)
            return false;
        groupLinkRepository.save(groupLink.setWishes(wishes));
        return false;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Participant getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return Participant.ANONYMOUS;
        Object o = authentication.getPrincipal();
        if (o instanceof Participant participant) {
            return participant;
        }
        return Participant.ANONYMOUS;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public Participant updateCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return Participant.ANONYMOUS;
        Object o = authentication.getPrincipal();
        if (o instanceof Participant participant) {
            Participant p = findById(participant.getUuid());
            if (participant.equals(p)) {
                participant = p;
                PreAuthenticatedAuthenticationToken auth = new PreAuthenticatedAuthenticationToken(
                        participant,
                        participant.getPassword(),
                        participant.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            return participant;
        }
        return Participant.ANONYMOUS;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public ParticipantDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Participant participant = findByEmail(email);
        if (participant == null)
            throw new UsernameNotFoundException(email + " not found");
        return participant;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public void sendResetPasswordLink(String email, TimeZone timeZone, Locale locale) throws ServiceException {
        Participant participant = findByEmail(email);
        if (participant == null)
            throw new ServiceException(ExceptionCode.PARTICIPANT_NOT_EXIST, email);
        if (staticContext.isCustomPassword()) {
            String actionToken = UUID.randomUUID().toString();
            participantRepository.save(
                    participant.setServiceActionToken(actionToken)
                            .setServiceActionTokenDate(LocalDateTime.now())
            );
            notificationEventFactory.fireNotification(
                    email,
                    staticContext.getMailCommonSubject(locale),
                    staticContext.getTextTemplatePasswordResetLink(
                            email,
                            actionToken,
                            locale
                    )
            );
        } else {
            participantRepository.save(participant.setPassword(
                    passwordFactory.createPassword("will be generated", participant.getEmail(), locale)
            ));
        }
    }

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION, exceptionMessage = EXCEPTION_MESSAGE_TEMPLATE)
    @Transactional
    @Override
    public boolean resetPassword(String email,
                                 String actionToken,
                                 String newPassword,
                                 TimeZone timeZone,
                                 Locale locale)
            throws ServiceException
    {
        Participant participant = findByEmail(email);
        if (participant == null)
            throw new ServiceException(ExceptionCode.PARTICIPANT_NOT_EXIST, email);
        if (actionToken == null || !actionToken.equals(participant.getServiceActionToken()))
            throw new ServiceException(ExceptionCode.ACTION_TOKEN_WRONG);
        if (participant.getServiceActionTokenDate() == null
                || ChronoUnit.SECONDS.between(participant.getServiceActionTokenDate(), ZonedDateTime.now()) > staticContext.getActionTokenDuration().getSeconds())
            throw new ServiceException(ExceptionCode.ACTION_TOKEN_EXPIRED);
        participantRepository.save(participant.setServiceActionToken(UUID.randomUUID().toString())
                .setPassword(passwordFactory.createPassword(newPassword, participant.getEmail(), locale)));
        return true;
    }
}
