package io.getarrayus.securecapita.controller;


import io.getarrayus.securecapita.dto.*;
import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.event.NewUserEvent;

import io.getarrayus.securecapita.exception.ApiException;
import io.getarrayus.securecapita.payload.LoginDto;
import io.getarrayus.securecapita.payload.UserDto;

import io.getarrayus.securecapita.repository.UserRepository;
import io.getarrayus.securecapita.security.JwtTokenProvider;
import io.getarrayus.securecapita.service.*;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.getarrayus.securecapita.constant.Constants.TOKEN_PREFIX;
import static io.getarrayus.securecapita.dto.UserDTOMapper.toUser;
import static io.getarrayus.securecapita.enumeration.EventType.*;

import static io.getarrayus.securecapita.utils.UserUtils.getAuthenticatedUser;

import static io.getarrayus.securecapita.utils.UserUtils.getLoggedInUser;
import static java.time.LocalTime.now;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;
import static org.springframework.util.MimeTypeUtils.IMAGE_PNG_VALUE;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

@RestController
@RequestMapping("/auth/secureapi")
@RequiredArgsConstructor
public class UserController {

    private  final UserService userService;
    private final  UserEventsService userEventsService;
    private final RolesService rolesService;
    private final UserRolesService userRolesService;
    private final  EventsService eventsService;
    private final  JwtTokenProvider jwtTokenProvider;

    private final ApplicationEventPublisher publisher;
    private final  ModelMapper mapper;
    private final  UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid UserDto userDto) throws InterruptedException {
        TimeUnit.SECONDS.sleep(4);
         UserDto userDto1 = userService.createUser(userDto);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDto1))
                        .message(String.format("User account created for user %s", userDto1.getFirstName()))
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build());
    }

    @PostMapping(value = {"/login", "/signin"})
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginDto loginDto){
        UserDto userDto = authenticate(loginDto.getEmail(),loginDto.getPassword());
        return Optional.ofNullable(userDto.getUsingMfa()).orElse(false)
                                         ? sendVerificationCode(userDto)
                                         : sendResponse(userDto);
    }

    private UserDto authenticate(String email, String password) {
        UserDto userByEmail = userService.getUserByEmail(email);
        try {
            if (userByEmail != null) {
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT));
            }
            Authentication authentication = authenticationManager.authenticate(unauthenticated(email, password));
            UserDto loggedInUser = getLoggedInUser(authentication);
            if (!loggedInUser.getUsingMfa()) {
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_SUCCESS));
            }
            return loggedInUser;
        } catch (Exception exception) {
            if (userByEmail != null) {
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_FAILURE));
            }
            throw new ApiException(exception.getMessage());
        }
    }


    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){
        UserDto userResponse = userService.getUserByEmail(getAuthenticatedUser(authentication).getEmail());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userResponse, "events", userEventsService.getEventsByUserId(userResponse.getId()), "roles", rolesService.getRoles()))
                        .message("Profile Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());

    }

    @PatchMapping("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateForm updateForm) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        UserDto updateUserDto = userService.updateUserDetails(updateForm);
        publisher.publishEvent(new NewUserEvent(updateUserDto.getEmail(), PROFILE_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", updateUserDto, "events", userEventsService.getEventsByUserId(updateUserDto.getId()), "roles", rolesService.getRoles()))
                        .message("User updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    // START - To reset password when user is not logged in
    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(
              @PathVariable("email") String email,
              @PathVariable("code") String code
    ){
        UserDto userDto=userService.verifyCode(email,code);
        publisher.publishEvent(new NewUserEvent(userDto.getEmail(),LOGIN_ATTEMPT_SUCCESS));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDto, "access_token", jwtTokenProvider.createAccessToken(getUserPrincipal(userDto))
                                , "refresh_token", jwtTokenProvider.createRefreshToken(getUserPrincipal(userDto))))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email){
        userService.resetPassword(email);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Email sent. Please check your email to reset your password.")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(
            @PathVariable("key") String key) throws InterruptedException
    {
        TimeUnit.SECONDS.sleep(3);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message(userService.verifyAccountKey(key).getEnabled() ? "Account already verified" : "Account verified")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordUrl(
                 @PathVariable("key") String key) throws InterruptedException
    {
        TimeUnit.SECONDS.sleep(3);
        UserDto userDto = userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDto))
                        .message("Please enter a new password")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PutMapping("/new/password")
    public ResponseEntity<HttpResponse> resetPasswordWithKey(@RequestBody @Valid NewPasswordForm form) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        userService.updatePasswordWithIdUser(form.getUserId(), form.getPassword(), form.getConfirmPassword());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password reset successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }
    // END - To reset password when user is not logged in

    @PatchMapping("/update/password")
    public ResponseEntity<HttpResponse> updatePassword(Authentication authentication, @RequestBody @Valid UpdatePasswordForm form) {
        UserDto userResponse = getAuthenticatedUser(authentication);
        userService.updatePassword(userResponse.getId(), form.getCurrentPassword(), form.getNewPassword(), form.getConfirmNewPassword());
        publisher.publishEvent(new NewUserEvent(userResponse.getEmail(), PASSWORD_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userService.getUserById(userResponse.getId()), "events", userEventsService.getEventsByUserId(userResponse.getId()), "roles", rolesService.getRoles()))
                        .message("Password updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    // update user role when the user is logging
    @PatchMapping("/update/role/{roleName}")
    public ResponseEntity<HttpResponse> updateUserRole(Authentication authentication, @PathVariable("roleName") String roleName) {
        UserDto userDTO = getAuthenticatedUser(authentication);
        userService.updateUserRole(userDTO.getId(), roleName);
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), ROLE_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(Map.of("user", userService.getUserById(userDTO.getId()), "events", userEventsService.getEventsByUserId(userDTO.getId()), "roles", rolesService.getRoles()))
                        .timeStamp(now().toString())
                        .message("Role updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/settings")
    public ResponseEntity<HttpResponse> updateAccountSettings(Authentication authentication, @RequestBody @Valid SettingsForm form) {
        UserDto userResponse = getAuthenticatedUser(authentication);
        userService.updateAccountSettings(userResponse.getId(), form.getEnabled(), form.getNotLocked());
        publisher.publishEvent(new NewUserEvent(userResponse.getEmail(), ACCOUNT_SETTINGS_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(Map.of("user", userService.getUserById(userResponse.getId()), "events", userEventsService.getEventsByUserId(userResponse.getId()), "roles", rolesService.getRoles()))
                        .timeStamp(now().toString())
                        .message("Account settings updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/togglemfa")
    public ResponseEntity<HttpResponse> toggleMfa(Authentication authentication) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        UserDto user = userService.toggleMfa(getAuthenticatedUser(authentication).getEmail());
        publisher.publishEvent(new NewUserEvent(user.getEmail(), MFA_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(Map.of("user", user, "events", userEventsService.getEventsByUserId(user.getId()), "roles", rolesService.getRoles()))
                        .timeStamp(now().toString())
                        .message("Multi-Factor Authentication updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/image")
    public ResponseEntity<HttpResponse> updateProfileImage(Authentication authentication, @RequestParam("image") MultipartFile image) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        UserDto user = getAuthenticatedUser(authentication);
        userService.updateImage(user, image);
        publisher.publishEvent(new NewUserEvent(user.getEmail(), PROFILE_PICTURE_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(Map.of("user", userService.getUserById(user.getId()), "events", userEventsService.getEventsByUserId(user.getId()), "roles", rolesService.getRoles()))
                        .timeStamp(now().toString())
                        .message("Profile image updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping(value = "/image/{fileName}", produces = IMAGE_PNG_VALUE)
    public byte[] getProfileImage(@PathVariable("fileName") String fileName) throws Exception {
        return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/IdeaProjects/securecapita/src/main/resources/imagesProfiles/" + fileName));
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDto userDto){
        System.out.println("Send Verification code");
         userService.sendVerificationCode(userDto);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDto))
                        .message("Verification Code Sent")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDto userDto){
        System.out.println("Send response ");
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDto, "access_token",jwtTokenProvider.createAccessToken(getUserPrincipal(userDto))
                                , "refresh_token", jwtTokenProvider.createRefreshToken(getUserPrincipal(userDto))))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    private UserPrincipal getUserPrincipal(UserDto user) {
        UserDto userResponse = userService.getUserByEmail(user.getEmail());
        UserDto userDto=mapper.map(userResponse,UserDto.class);
        return new UserPrincipal(mapper.map(userDto, Users.class), mapper.map(rolesService.getRoleByUserId(userResponse.getId()), Roles.class));
    }

    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request) {
        if(isHeaderAndTokenValid(request)) {
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDto user = userService.getUserById(jwtTokenProvider.getSubject(token, request));
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .data(Map.of("user", user, "access_token", jwtTokenProvider.createAccessToken(getUserPrincipal(user))
                                    , "refresh_token", token))
                            .message("Token refreshed")
                            .status(OK)
                            .statusCode(OK.value())
                            .build());
        } else {
            return ResponseEntity.badRequest().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .reason("Refresh Token missing or invalid")
                            .developerMessage("Refresh Token missing or invalid")
                            .status(BAD_REQUEST)
                            .statusCode(BAD_REQUEST.value())
                            .build());
        }
    }

    private boolean isHeaderAndTokenValid(HttpServletRequest request) {
        return  request.getHeader(AUTHORIZATION) != null
                &&  request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)
                && jwtTokenProvider.isTokenValid(
                jwtTokenProvider.getSubject(request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()), request),
                request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length())
        );
    }

    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("There is no mapping for a " + request.getMethod() + " request for this path on the server")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build());
    }

    private URI getUri() {
        return URI.create(fromCurrentContextPath().path("/auth/secureapi/get/<userId>").toUriString());
    }


}
