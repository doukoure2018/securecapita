package io.getarrayus.securecapita.controller;

import io.getarrayus.securecapita.dto.CampaignsDto;
import io.getarrayus.securecapita.dto.HttpResponse;
import io.getarrayus.securecapita.dto.SmsMessagesDto;
import io.getarrayus.securecapita.entity.SmsMessages;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.service.CampaignsService;
import io.getarrayus.securecapita.service.SmsMessagesService;
import io.getarrayus.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;

import static java.time.LocalTime.now;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(path = "/secureapi/message")
@RequiredArgsConstructor
public class SmsMessagesController {

    private final SmsMessagesService smsMessagesService;
    private final UserService userService;
    private final CampaignsService campaignsService;

    @PostMapping("/{campaign_id}/{id_user}/create")
    public ResponseEntity<HttpResponse> importMessage(@AuthenticationPrincipal UserDto user, @RequestParam("file") MultipartFile importFile, @PathVariable(name = "campaign_id") Long campaign_id,@PathVariable(name = "id_user") Long id_user) {
         smsMessagesService.importCampaignFiles(importFile,campaign_id,id_user);
        return ResponseEntity.created(URI.create(""))
                .body(
                        HttpResponse.builder()
                                .timeStamp(now().toString())
                                .data(Map.of("user", userService.getUserByEmail(user.getEmail()),
                                        "Campaign", campaignsService.getCampaign(campaign_id)))
                                .message("Data Imported Successfully")
                                .status(CREATED)
                                .statusCode(CREATED.value())
                                .build());
    }
}
