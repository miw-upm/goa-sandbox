package es.upm.api.adapter.out.user.feign;

import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.ports.out.user.UserFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserFinderAdapter implements UserFinder {
    private final GoaUserClient goaUserClient;

    @Override
    public UserSnapshot readById(UUID id) {
        return goaUserClient.readUserById(id);
    }

    @Override
    public UserSnapshot readByMobile(String mobile) {
        return goaUserClient.readUserByMobile(mobile);
    }

    @Override
    public List<UserSnapshot> find(String attribute) {
        return goaUserClient.findUser(attribute);
    }
}
