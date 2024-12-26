package com.example.account.controller;

import com.example.account.dto.req.CreateAccountReq;
import com.example.account.dto.resp.ApiResponse;
import com.example.account.service.IServices;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class Controller {
    private final Keycloak keycloak;
    private final IServices services;

    @PostMapping("/user/create")
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateAccountReq request) {
        try {
            services.createUser(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "User created successfully", null));
    }
}
