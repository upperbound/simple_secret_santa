package com.github.upperbound.secret_santa.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.repository.GroupRepository;
import com.github.upperbound.secret_santa.repository.ParticipantRepository;
import com.github.upperbound.secret_santa.service.GroupService;
import com.github.upperbound.secret_santa.service.MailService;
import com.github.upperbound.secret_santa.service.ServiceException;
import com.github.upperbound.secret_santa.util.ApplicationParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class GroupServiceImpl implements GroupService {
    private final ApplicationParams applicationParams;
    private final MailService mailService;
    private final GroupRepository groupRepository;
    private final ParticipantRepository participantRepository;

    public Group update(Group group) {
        return groupRepository.save(group);
    }

    public boolean delete(String uuid) {
        Group g = groupRepository.findById(uuid).orElse(null);
        if (g == null)
            return false;
        participantRepository.deleteAll(participantRepository.findAllByGroup(g));
        groupRepository.delete(g);
        return true;
    }

    public Group findById(String uuid) {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "findById";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            return groupRepository.findById(uuid).orElse(null);
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    public Group findByDescription(String name) {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "findByDescription";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            return groupRepository.findByDescription(name).orElse(null);
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    public List<Group> findAll() {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "findAll";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            return groupRepository.findAll();
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    public List<Group> findAllByHasDrawnFalse() {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "findAll";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            return groupRepository.findAllByHasDrawnFalse();
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    public Group createIfNotExist(String description) {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "createIfNotExist";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            Group group = groupRepository.findByDescription(description).orElse(new Group());
            if (group.getUuid() != null)
                return group;
            log.info("creating new group '{}'", description);
            group.setDescription(description);
            return groupRepository.save(group);
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    public boolean resetDraw(Group group) throws ServiceException {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "resetDraw";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            if (!group.getHasDrawn())
                return false;
            group.setHasDrawn(false);
            groupRepository.save(group);
            List<Participant> participants = participantRepository.findAllByGroup(group);
            if (participants.isEmpty())
                return false;
            log.info("reset draw results on '{}'", group.getDescription());
            participants.forEach(participant -> {
                participant.setToGiftTo(null);
                participantRepository.save(participant);
            });
            groupRepository.flush();
            participantRepository.flush();
            for (Participant participant : participants) {
                if (participant.getReceiveNotifications())
                    mailService.sendMessage(
                            participant.getEmail(),
                            applicationParams.getMailCommonSubject(participant.getGroup().getDescription()),
                            applicationParams.getMailDrawCancelText()
                    );
            }
            return true;
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }

    public boolean makeADraw(Group group) throws ServiceException {
        String mdcOld = MDC.get(applicationParams.MDC_SERVICE_ACTION), actionName = "makeADraw";
        try {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld == null ? actionName : mdcOld + "." + actionName);
            if (group.getHasDrawn())
                return false;
            List<Participant> participants = participantRepository.findAllByGroup(group);
            if (participants.isEmpty())
                return false;
            log.info("making a draw for '{}'", group.getDescription());
            List<Participant> result = new ArrayList<>(participants.size());
            Random random = new Random();
            Participant first = participants.get(random.nextInt(0, participants.size()));
            result.add(first);
            participants.remove(first);
            Participant current = first;
            while (!participants.isEmpty()) {
                Participant toGiftTo = participants.get(random.nextInt(0, participants.size()));
                current.setToGiftTo(toGiftTo);
                log.trace("user '{}' draw to_gift_to '{}'", current.getUuid(), toGiftTo.getUuid());
                participantRepository.save(current);
                participants.remove(toGiftTo);
                current = toGiftTo;
                result.add(current);
            }
            current.setToGiftTo(first);
            log.trace("user '{}' draw to_gift_to '{}'", current.getUuid(), first.getUuid());
            participantRepository.save(current);
            group.setHasDrawn(true);
            groupRepository.save(group);
            groupRepository.flush();
            participantRepository.flush();
            for (Participant participant : result) {
                if (participant.getReceiveNotifications())
                    mailService.sendMessage(
                            participant.getEmail(),
                            applicationParams.getMailCommonSubject(participant.getGroup().getDescription()),
                            applicationParams.getMailDrawResultsText(
                                    participant.getToGiftTo().getEmail(),
                                    participant.getToGiftTo().getInfo(),
                                    participant.getToGiftTo().getWishes()
                            )
                    );
            }
            return true;
        } catch (RuntimeException e) {
            log.error("unable to perform action", e);
            throw e;
        } finally {
            MDC.put(applicationParams.MDC_SERVICE_ACTION, mdcOld);
        }
    }
}
