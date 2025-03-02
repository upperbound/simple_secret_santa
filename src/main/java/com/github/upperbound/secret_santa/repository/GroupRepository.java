package com.github.upperbound.secret_santa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.github.upperbound.secret_santa.model.Group;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    Optional<Group> findByDescription(String description);
    List<Group> findAllByHasDrawnFalse();
}