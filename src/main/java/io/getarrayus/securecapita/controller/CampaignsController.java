package io.getarrayus.securecapita.controller;

import io.getarrayus.securecapita.domain.Customer;
import io.getarrayus.securecapita.dto.CampaignsDto;
import io.getarrayus.securecapita.dto.HttpResponse;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.service.CampaignsService;
import io.getarrayus.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

import static java.time.LocalTime.now;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(path = "/secureapi/campaign")
@RequiredArgsConstructor
public class CampaignsController {

    private final CampaignsService campaignsService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<HttpResponse> createCampaign(@AuthenticationPrincipal UserDto user, @RequestBody CampaignsDto campaignsDto) {

        //CampaignsDto responseCamapaign = campaignsService.create(campaignsDto);
        return ResponseEntity.created(URI.create(""))
                .body(
                        HttpResponse.builder()
                                .timeStamp(now().toString())
                                .data(Map.of("user", userService.getUserByEmail(user.getEmail()),
                                        "Campaign", campaignsService.create(campaignsDto)))
                                .message("Customer created")
                                .status(CREATED)
                                .statusCode(CREATED.value())
                                .build());
    }

}
