package io.getarrayus.securecapita.service.impl;

import io.getarrayus.securecapita.service.OrangeSmsService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
public class OrangeSmsServiceImpl implements OrangeSmsService {


    @Override
    // Method to get OAuth token from Orange API
    public String getOAuthToken() {
        try {
            HttpResponse<String> response = Unirest.post("https://api.orange.com/oauth/v3/token")
                    .header("Authorization", "Basic c3FMYnVoc08yYW03eDJhaUUycnNvM0lDR0ZJZnZVZzE6V3RqVU5kR3lWUTVtZ281aQ==") // Use your credentials
                    //.header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .field("grant_type", "client_credentials")
                    .asString();

            // Extract the access token from the response
            System.out.println(response.getBody());
            String token = extractToken(response.getBody());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractToken(String responseBody) throws IOException {
        System.out.println(responseBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (jsonNode.has("access_token")) {
            return jsonNode.get("access_token").asText();
        } else {
            throw new IllegalArgumentException("No 'access_token' field found in response");
        }
    }

    @Override
    public void sendSms(String token, String recipient, String senderName, String message) {
        // Escape newline characters
        String escapedMessage = message.replace("\n", "\\n").replace("\"", "\\\"");

        try {
            HttpResponse<String> response = Unirest.post("https://api.orange.com/smsmessaging/v1/outbound/tel%3A%2B224622459305/requests")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .body("{\"outboundSMSMessageRequest\":{\"address\":\"tel:+224" + recipient + "\",\"senderAddress\":\"tel:+224622459305\",\"senderName\":\"" + senderName + "\",\"outboundSMSTextMessage\":{\"message\":\"" + escapedMessage + "\"}}}")
                    .asString();
            if (response.getStatus() == 201) {
                System.out.println("SMS sent successfully!");
            } else {
                System.out.println("Failed to send SMS: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
