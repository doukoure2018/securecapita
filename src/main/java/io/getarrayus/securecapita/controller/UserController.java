package io.getarrayus.securecapita.controller;

import com.twilio.exception.ApiException;
import com.twilio.http.Response;
import io.getarrayus.securecapita.dto.*;
import io.getarrayus.securecapita.enumeration.EventType;
import io.getarrayus.securecapita.event.NewUserEvent;
import io.getarrayus.securecapita.payload.LoginDto;
import io.getarrayus.securecapita.payload.RolesDto;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.security.JwtTokenProvider;
import io.getarrayus.securecapita.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.apache.catalina.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.getarrayus.securecapita.enumeration.EventType.*;
import static io.getarrayus.securecapita.utils.ExceptionUtils.processError;
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
@AllArgsConstructor
public class UserController {
    private AuthenticationManager authenticationManager;
    private UserService userService;
    private UserEventsService userEventsService;
    private RolesService rolesService;
    private UserRolesService userRolesService;
    private EventsService eventsService;
    private JwtTokenProvider jwtTokenProvider;

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private ApplicationEventPublisher publisher;

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
        String token = userService.login(loginDto);
        String refreshToken =userService.generateRefreshToken(loginDto.getEmail());
        //UserDto userDto = userService.getUserByEmail(loginDto.getEmail());
        UserDto userDto = authenticate(loginDto.getEmail(), loginDto.getPassword());

        return Optional.ofNullable(userDto.getUsingMfa()).orElse(false)
                                         ? sendVerificationCode(userDto)
                                         : sendResponse(token, refreshToken, userDto);
    }

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){
        UserDto userDto = userService.getUserByEmail(getAuthenticatedUser(authentication).getEmail());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDto, "events", userEventsService.getEventsByUserId(userDto.getId()), "roles", rolesService.getRoles()))
                        .message("Profile Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());

    }

    @PatchMapping("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateForm updateForm){
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
              @PathVariable("code") String code,
              Authentication authentication
    ){
        UserDto userDto=userService.verifyCode(email,code);
        publisher.publishEvent(new NewUserEvent(userDto.getEmail(),LOGIN_ATTEMPT_SUCCESS));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDto, "access_token", jwtTokenProvider.generateToken(authentication)
                                , "refresh_token", jwtTokenProvider.generateRefreshToken(authentication)))
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
        UserDto userDTO = getAuthenticatedUser(authentication);
        userService.updatePassword(userDTO.getId(), form.getCurrentPassword(), form.getNewPassword(), form.getConfirmNewPassword());
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), PASSWORD_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userService.getUserById(userDTO.getId()), "events", userEventsService.getEventsByUserId(userDTO.getId()), "roles", rolesService.getRoles()))
                        .message("Password updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    // update user role when the user is logging
    @PatchMapping("/update/role/{roleName}")
    public ResponseEntity<HttpResponse> updateUserRole(Authentication authentication, @PathVariable("roleName") String roleName) {
        UserDto userDTO = getAuthenticatedUser(authentication);
        userRolesService.updateUserRole(userDTO.getId(), roleName);
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
        UserDto userDTO = getAuthenticatedUser(authentication);
        userService.updateAccountSettings(userDTO.getId(), form.getEnabled(), form.getNotLocked());
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), ACCOUNT_SETTINGS_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(Map.of("user", userService.getUserById(userDTO.getId()), "events", userEventsService.getEventsByUserId(userDTO.getId()), "roles", rolesService.getRoles()))
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
        return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/Downloads/images/" + fileName));
    }

    private UserPrincipal getUserPrincipal(UserDto userDto){
        return new UserPrincipal(userDto,rolesService.getRoleByUserId(userDto.getId()));
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

    private ResponseEntity<HttpResponse> sendResponse(String token,String refreshToken, UserDto userDto){
        System.out.println("Send response ");
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDto, "access_token",token
                                , "refresh_token", refreshToken))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
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

    private UserDto authenticate(String email, String password) {
        UserDto userByEmail = userService.getUserByEmail(email);
        try {
            if(null != userByEmail) {
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT));
            }
            Authentication authentication = authenticationManager.authenticate(unauthenticated(email, password));

            UserDto loggedInUser = getLoggedInUser(authentication);
            if(!loggedInUser.getUsingMfa()) {
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_SUCCESS));
            }
            return loggedInUser;
        } catch (Exception exception) {
            if(null != userByEmail) {
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_FAILURE));
            }
            processError(request, response, exception);
            throw new ApiException(exception.getMessage());
        }
    }


    private URI getUri() {
        return URI.create(fromCurrentContextPath().path("/auth/secureapi/get/<userId>").toUriString());
    }
}
