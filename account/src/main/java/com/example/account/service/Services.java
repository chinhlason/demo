package com.example.account.service;

import com.example.account.dto.req.CreateAccountReq;
import com.example.account.dto.resp.ErrorResponse;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class Services implements IServices {
    @Value("${keycloak.realm}")
    private String realmName;

    private String ACCESS_TOKEN_KEY = "access_token";

    private final Keycloak keycloakAdmin;
    private final RedisTemplate<String, Object> redisTemplate;

    public String getAdminToken() {
        String accessToken = (String) redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
        if (accessToken == null) {
            String token = keycloakAdmin.tokenManager().getAccessTokenString();
            redisTemplate.opsForValue().
                    set(ACCESS_TOKEN_KEY, token,
                            keycloakAdmin.tokenManager().getAccessToken().getExpiresIn() - 5,
                            TimeUnit.SECONDS);
            return token;
        }
        return accessToken;
    }

    @Override
    public void createRealms() {
        keycloakAdmin.realms().findAll().forEach(realmRepresentation -> {
            if (realmRepresentation.getRealm().equals(realmName)) {
                return;
            }
        });
        RealmRepresentation request = new RealmRepresentation();
        request.setRealm(realmName);
        request.setEnabled(true);
        request.setDisplayName(realmName);
        request.setSslRequired("external");
        request.setAccessCodeLifespan(1800);
        keycloakAdmin.realms().create(request);
    }

    @Override
    public void createUser(CreateAccountReq request) throws RuntimeException {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(credential));

        Map<String, List<String>> userAttributes = new HashMap<>();
        userAttributes.put("balance", Collections.singletonList("0"));
        user.setAttributes(userAttributes);

        Response response = keycloakAdmin.realm(realmName).users().create(user);
        if (response.getStatus() != 201) {
            ErrorResponse err = response.readEntity(ErrorResponse.class);
            log.error("Error creating user: " + err.getErrorMessage());
            throw new RuntimeException(err.getErrorMessage());
        }
        log.info("User created with status: " + response.getStatus());
    }
}
