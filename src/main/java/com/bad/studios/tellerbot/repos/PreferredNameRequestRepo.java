package com.bad.studios.tellerbot.repos;

import com.bad.studios.tellerbot.models.PreferredNameRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferredNameRequestRepo extends JpaRepository<PreferredNameRequest, String> {
}
