package com.github.upperbound.secret_santa.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.model.ParticipantRole;
import com.github.upperbound.secret_santa.repository.GroupRepository;
import com.github.upperbound.secret_santa.repository.ParticipantRepository;
import com.github.upperbound.secret_santa.service.MailService;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.model.ParticipantDetails;
import com.github.upperbound.secret_santa.service.ServiceException;
import com.github.upperbound.secret_santa.util.ApplicationParams;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ParticipantServiceImpl implements ParticipantService {
    private final ApplicationParams applicationParams;
    private final MailService mailService;
    private final GroupRepository groupRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public Participant findById(String uuid) {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "findById";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            return participantRepository.findById(uuid).orElse(null);
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public List<Participant> findAll() {
        return participantRepository.findAll();
    }

    @Override
    public Participant findByEmail(String email) {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "findByEmail";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            return participantRepository.findByEmail(email).orElse(null);
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public List<Participant> findAllByGroup(Group group) {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "findAllByGroup";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            return participantRepository.findAllByGroup(group);
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public boolean updateRole(String email, ParticipantRole role) {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "updateRole";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            Participant participant = participantRepository.findByEmail(email).orElse(null);
            if (participant == null)
                return false;
            participant.setRole(role);
            participantRepository.save(participant);
            return true;
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public boolean swapRole(String uuid) {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "swapRole";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            Participant participant = participantRepository.findById(uuid).orElse(null);
            if (participant == null)
                return false;
            participant.setRole(participant.getRole().equals(ParticipantRole.USER) ? ParticipantRole.ADMIN : ParticipantRole.USER);
            participantRepository.save(participant);
            return true;
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public Participant createIfNotExists(String groupUuid,
                                         String email,
                                         String password,
                                         String info,
                                         String wishes,
                                         boolean receiveNotifications) throws ServiceException {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "createIfNotExists";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            Group g = groupRepository.findById(groupUuid).orElse(null);
            if (g == null)
                return null;
            return createIfNotExists(g, email, password, info, wishes, receiveNotifications);
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public Participant createIfNotExists(Group group,
                                         String email,
                                         String password,
                                         String info,
                                         String wishes,
                                         boolean receiveNotifications) throws ServiceException {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "createIfNotExists";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            Participant participant = participantRepository.findByEmail(email).orElse(new Participant());
            if (participant.getUuid() != null)
                return participant;
            log.info("creating new user:[{}, {}]", email, info);
            participant.setGroup(group);
            participant.setEmail(email);
//            participant.setPassword("1234");
            participant.setPassword(password == null || password.length() < 4 ? UUID.randomUUID().toString().substring(0, 6) : password);
            participant.setInfo(info);
            participant.setWishes(wishes);
            participant.setReceiveNotifications(receiveNotifications);
            participantRepository.save(participant);
            participantRepository.flush();
            sendCredentialsToUser(email);
            return participantRepository.save(participant);
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public Participant update(Participant participant) {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "update";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            return participantRepository.save(participant);
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public boolean delete(String uuid) throws ServiceException {
        return delete(uuid, true);
    }

    public boolean delete(String uuid, boolean delete) throws ServiceException {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "delete";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            Participant participant = participantRepository.findById(uuid).orElse(null);
            if (participant == null)
                return false;
            log.info(
                    "deleting participant '{}' from group '{}'",
                    participant.getUuid(),
                    participant.getGroup().getDescription()
            );
            if (!participant.getGroup().getHasDrawn()) {
                if (delete) {
                    participantRepository.delete(participant);
                    sendDeleteInfoToUser(participant);
                }
                return true;
            }
            Participant toGiftTo = participant.getToGiftTo();
            List<Participant> grandFatherFrost = participantRepository.findAllByToGiftTo(participant);
            for (Participant gff : grandFatherFrost) {
                gff.setToGiftTo(toGiftTo);
                participantRepository.save(gff);
                log.info(
                        "for participant '{}' added new toGift '{}'",
                        gff.getUuid(),
                        toGiftTo.getUuid()
                );
                mailService.sendMessage(
                        participant.getEmail(),
                        applicationParams.getMailCommonSubject(participant.getGroup().getDescription()),
                        applicationParams.getMailDrawChangesText(
                                toGiftTo.getEmail(),
                                toGiftTo.getInfo(),
                                toGiftTo.getWishes()
                        )
                );
            }
            if (delete) {
                participantRepository.delete(participant);
                sendDeleteInfoToUser(participant);
            }
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
        return true;
    }

    @Override
    public boolean moveParticipant(String uuid, Group targetGroup) throws ServiceException {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "moveParticipant";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            Participant participant = findById(uuid);
            if (participant == null)
                return false;
            Group currentGroup = participant.getGroup();
            if (targetGroup == null)
                return false;
            if (currentGroup.equals(targetGroup))
                return false;
            if (currentGroup.getHasDrawn() && !delete(participant.getUuid(), false))
                return false;
            log.info(
                    "moving participant '{}' from group '{}' to group '{}'",
                    participant.getEmail(),
                    currentGroup.getDescription(),
                    targetGroup.getDescription()
            );
            participant.setToGiftTo(null);
            Participant forReplace = null;
            if (targetGroup.getHasDrawn()) {
                List<Participant> targetGroupParticipants = findAllByGroup(targetGroup);
                targetGroupParticipants.remove(getCurrentAuthentication());
                if (!targetGroupParticipants.isEmpty()) {
                    Random random = new Random();
                    forReplace = targetGroupParticipants.get(random.nextInt(0, targetGroupParticipants.size()));
                    participant.setToGiftTo(forReplace.getToGiftTo());
                    forReplace.setToGiftTo(participant);
                    participantRepository.save(forReplace);
                }
            }
            participant.setGroup(targetGroup);
            participantRepository.save(participant);
            participantRepository.flush();
            if (forReplace != null) {
                mailService.sendMessage(
                        participant.getEmail(),
                        applicationParams.getMailCommonSubject(participant.getGroup().getDescription()),
                        applicationParams.getMailDrawChangesText(
                                participant.getToGiftTo().getEmail(),
                                participant.getToGiftTo().getInfo(),
                                participant.getToGiftTo().getWishes()
                        )
                );
                mailService.sendMessage(
                        forReplace.getEmail(),
                        applicationParams.getMailCommonSubject(forReplace.getGroup().getDescription()),
                        applicationParams.getMailDrawChangesText(
                                forReplace.getToGiftTo().getEmail(),
                                forReplace.getToGiftTo().getInfo(),
                                forReplace.getToGiftTo().getWishes()
                        )
                );
            }
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
        return true;
    }

    @Override
    public Participant getCurrentAuthentication() {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "getCurrentAuthentication";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null)
                return Participant.ANONYMOUS;
            Object o = authentication.getPrincipal();
            if (o instanceof Participant participant) {
                Participant p = findById(participant.getUuid());
                return p == null ? Participant.ANONYMOUS : p;
            }
            return Participant.ANONYMOUS;
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public ParticipantDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "loadUserByUsername";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            Participant participant = findByEmail(email);
            if (participant == null)
                throw new UsernameNotFoundException(email + " not found");
            return participant;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    @Override
    public void sendCredentialsToUser(String email) throws ServiceException {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "sendCredentialsToUser";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            ParticipantDetails participantDetails = loadUserByUsername(email);
            mailService.sendMessage(
                    email,
                    applicationParams.getMailCommonSubject(participantDetails.getGroup().getDescription()),
                    applicationParams.getMailParticipantCreditsText(email, participantDetails.getPassword())
            );
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    public boolean sendDeleteInfoToUser(Participant participant) throws ServiceException {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "sendDeleteInfoToUser";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            mailService.sendMessage(
                    participant.getEmail(),
                    applicationParams.getMailCommonSubject(participant.getGroup().getDescription()),
                    applicationParams.getMailParticipantDeletedText(participant.getGroup().getHasDrawn())
            );
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
        return true;
    }
}
