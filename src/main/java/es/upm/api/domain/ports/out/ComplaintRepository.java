package es.upm.api.domain.ports.out;

import es.upm.api.domain.model.Complaint;
import java.util.Optional;

public interface ComplaintRepository {
    // para crear la queja
    Complaint create(Complaint complaint);

    // para verificar si ya existe una queja con ese ID (barcode-user-state)
    Optional<Complaint> read(String id);
}