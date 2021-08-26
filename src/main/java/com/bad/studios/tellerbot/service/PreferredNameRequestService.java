package com.bad.studios.tellerbot.service;

import com.bad.studios.tellerbot.models.PreferredNameRequest;
import com.bad.studios.tellerbot.repos.PreferredNameRequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PreferredNameRequestService {

    private PreferredNameRequestRepo repo;

    @Autowired
    public PreferredNameRequestService(PreferredNameRequestRepo repo) {
        this.repo = repo;
    }

    public PreferredNameRequest savePreferredNameRequest(PreferredNameRequest request) {
        return repo.save(request);
    }

    public Boolean getPreferredNameRequest(String id) {
        return repo.findById(id).isPresent();
    }

    public void deletePreferredNameRequest(String id) {
        repo.deleteById(id);
    }

}
