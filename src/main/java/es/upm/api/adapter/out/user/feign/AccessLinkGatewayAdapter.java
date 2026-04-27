package es.upm.api.adapter.out.user.feign;

import es.upm.api.domain.model.external.AccessLinkSnapshot;
import es.upm.api.domain.ports.out.user.AccessLinkGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessLinkGatewayAdapter implements AccessLinkGateway {
    private final GoaUserClient goaUserClient;

    @Override
    public AccessLinkSnapshot use(String id, String mobile, String scope) {
        return goaUserClient.useAccessLink(id, mobile, scope);
    }
}
