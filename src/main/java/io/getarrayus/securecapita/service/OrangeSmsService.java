package io.getarrayus.securecapita.service;

public interface OrangeSmsService {

    public String getOAuthToken();

    public void sendSms(String token, String recipient, String senderName, String message);
}
