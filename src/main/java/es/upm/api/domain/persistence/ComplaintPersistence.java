package es.upm.api.domain.persistence;

import es.upm.api.domain.model.Complaint;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintPersistence {
    void create(Complaint complaint);
}