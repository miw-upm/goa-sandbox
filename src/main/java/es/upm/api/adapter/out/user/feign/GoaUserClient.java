package es.upm.api.adapter.out.user.feign;

import es.upm.api.configurations.FeignConfig;
import es.upm.api.domain.model.external.AccessLinkSnapshot;
import es.upm.api.domain.model.external.UserSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = GoaUserClient.GOA_USER, configuration = FeignConfig.class)
public interface GoaUserClient {
    String GOA_USER = "goa-user";
    String USERS = "/users";
    String ACCESS_LINK = "/access-link";
    String ID_ID = "/{id}";
    String MOBILE_ID = "/{mobile}";

    @GetMapping(USERS + ID_ID)
    UserSnapshot readUserById(@PathVariable UUID id);

    @GetMapping(USERS + MOBILE_ID)
    UserSnapshot readUserByMobile(@PathVariable String mobile);

    @GetMapping(USERS)
    List<UserSnapshot> findUser(@RequestParam(required = false) String attribute);

    @PostMapping(ACCESS_LINK + ID_ID)
    AccessLinkSnapshot useAccessLink(@PathVariable String id, @RequestParam String mobile, @RequestParam String scope);

}
