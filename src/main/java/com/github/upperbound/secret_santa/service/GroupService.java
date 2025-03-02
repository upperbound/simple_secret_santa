package com.github.upperbound.secret_santa.service;

import com.github.upperbound.secret_santa.model.Group;

import java.util.List;

public interface GroupService {
    Group update(Group group);
    boolean delete(String uuid);
    Group findById(String uuid);
    Group findByDescription(String name);
    List<Group> findAll();
    List<Group> findAllByHasDrawnFalse();
    Group createIfNotExist(String description);
    boolean resetDraw(Group group) throws ServiceException;
    boolean makeADraw(Group group) throws ServiceException;

}
