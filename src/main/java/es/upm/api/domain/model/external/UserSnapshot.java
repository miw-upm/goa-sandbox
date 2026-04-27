package es.upm.api.domain.model.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSnapshot {
    public static final String SIN_DEFINIR = "(___SIN DEFINIR___)";
    private UUID id;
    private String mobile;
    private String firstName;
    private String familyName;
    private String documentType;
    private String identity;
    private String email;

    public String toFullName() {
        return firstName + " " + valueOrUndefined(familyName);
    }

    public String toDonFullName() {
        return "D./Dña. " + this.toFullName();
    }

    public String toDonFullNameAndIdentity() {
        return this.toDonFullName() + " con N.I.F nº " + valueOrUndefined(identity);
    }

    public boolean isComplete() {
        return this.id != null
                && this.mobile != null
                && this.firstName != null
                && this.familyName != null
                && this.documentType != null
                && this.identity != null
                && this.email != null;
    }

    private String formatDocumentType() {
        if (documentType == null) {
            return SIN_DEFINIR;
        }
        return String.join(".", documentType.toUpperCase().split("")) + ".";
    }

    private String valueOrUndefined(String value) {
        return value != null ? value : SIN_DEFINIR;
    }
}
