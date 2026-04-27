package es.upm.api.adapter.out.legal.mongo.customerfiledownload;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerFileDownloadRepository extends MongoRepository<CustomerFileDownloadEntity, UUID> {
    List<CustomerFileDownloadEntity> findByCustomerIdIn(List<UUID> customerIds);

    List<CustomerFileDownloadEntity> findByDocumentTypeContainingIgnoreCase(String documentType);

    List<CustomerFileDownloadEntity> findByCustomerIdInAndDocumentTypeContainingIgnoreCase(List<UUID> customerIds, String documentType);
}
