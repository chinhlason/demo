package com.example.account.service;

import com.example.account.dto.req.CreateAccountReq;
import org.springframework.stereotype.Service;

@Service
public interface IServices {
    void createRealms();
    void createUser(CreateAccountReq request);
}
