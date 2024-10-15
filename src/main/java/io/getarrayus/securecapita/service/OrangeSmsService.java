package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.dto.TokenResponse;

public interface OrangeSmsService {

    public TokenResponse getOAuthToken();

    public void sendSms(String token, String recipient, String senderName, String message);
}
