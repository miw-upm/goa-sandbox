package es.upm.api.domain.services;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.persistence.ComplaintPersistence;
import es.upm.api.domain.webclients.EngagementWebClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ComplaintService {

    private final ComplaintPersistence complaintPersistence;
    private final EngagementWebClient engagementWebClient;

    public ComplaintService(ComplaintPersistence complaintPersistence, EngagementWebClient engagementWebClient) {
        this.complaintPersistence = complaintPersistence;
        this.engagementWebClient = engagementWebClient;
    }

    public Complaint create(Complaint complaint) {
        complaint.setId(UUID.randomUUID());
        this.engagementWebClient.readById(complaint.getEngagementId());
        this.complaintPersistence.create(complaint);
        return complaint;
    }

    public Stream<Complaint> findAll() {
        return this.complaintPersistence.findAll();
    }

    public Complaint readById(UUID id) {
        return this.complaintPersistence.readById(id);
    }
}