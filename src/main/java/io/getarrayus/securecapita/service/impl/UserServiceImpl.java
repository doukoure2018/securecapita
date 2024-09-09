package io.getarrayus.securecapita.service.impl;


import io.getarrayus.securecapita.dto.UpdateForm;
import io.getarrayus.securecapita.dto.UserResponse;
import io.getarrayus.securecapita.entity.*;
import io.getarrayus.securecapita.enumeration.VerificationType;
import io.getarrayus.securecapita.event.NewUserEvent;
import io.getarrayus.securecapita.exception.ApiException;
import io.getarrayus.securecapita.exception.BlogAPIException;
import io.getarrayus.securecapita.exception.ResourceNotFoundException;
import io.getarrayus.securecapita.payload.LoginDto;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.repository.*;
import io.getarrayus.securecapita.service.EmailService;
import io.getarrayus.securecapita.service.RolesService;
import io.getarrayus.securecapita.service.UserService;
import io.getarrayus.securecapita.utils.DateUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static io.getarrayus.securecapita.enumeration.EventType.*;
import static io.getarrayus.securecapita.enumeration.VerificationType.ACCOUNT;

import static io.getarrayus.securecapita.enumeration.VerificationType.PASSWORD;

import static io.getarrayus.securecapita.utils.UserUtils.getLoggedInUser;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl  implements UserService {

    private static  String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private  AuthenticationManager authenticationManager;
    private  UserRepository userRepository;
    private  TwoFactorVerificationsRepository twoFactorVerificationsRepository;
    private  ModelMapper mapper;
    private  PasswordEncoder passwordEncoder;
    private  RolesRepository rolesRepository;
    private  AccountVerificationsRepository accountVerificationsRepository;
    private  EmailService emailService;
    private  ResetPasswordVerificationRepository resetPasswordVerificationsRepository;
    private  ApplicationEventPublisher publisher;
    private  RolesService rolesService;

    @Override
    public UserResponse createUser(UserDto userDto) {

        if(userRepository.existsByEmail(userDto.getEmail())){
             throw new BlogAPIException(HttpStatus.BAD_REQUEST,"Email already exist");
        }
        Users user = new Users();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());

        user.setAddress(userDto.getAddress());
        user.setPhone(userDto.getPhone());
        user.setTitle(userDto.getTitle());
        user.setBio(userDto.getBio());
        user.setEnabled(false);
        user.setNonLocked(true);
        user.setUsingMfa(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setImageUrl("");
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        // add Roles
        Set<UserRoles> userRolesSet = new HashSet<>();
        Optional<Roles> userRoleOpt = rolesRepository.findByName("ROLE_USER");
        if (userRoleOpt.isPresent()) {
            UserRoles userRole = new UserRoles();
            userRole.setUser(user);
            userRole.setRole(userRoleOpt.get());
            userRolesSet.add(userRole);
        } else {
            throw new BlogAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "User role not found");
        }
        user.setUserRoles(userRolesSet);
        // save Information
        userRepository.save(user);
        //Insert verication account
        String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
        AccountVerifications accountVerifications = new AccountVerifications();
        accountVerifications.setUser(user);
        accountVerifications.setUrl(verificationUrl);
        accountVerificationsRepository.save(accountVerifications);
        // Send Email
         sendEmail(userDto.getFirstName(), userDto.getEmail(), verificationUrl,ACCOUNT);
         System.out.println(verificationUrl);
         // return the user information
        // Map user to UserResponse
        UserResponse userResponse = mapper.map(user, UserResponse.class);

        // Explicitly set the role and permissions in the response
        userResponse.setRoleName(userRoleOpt.get().getName());
        userResponse.setPermissions(userRoleOpt.get().getPermission());

        System.out.println(userResponse.getRoleName());
        System.out.println(userResponse.getPermissions());

        return userResponse;

    }

    @Override
    public UserResponse login(LoginDto loginDto)
    {
        return authenticate(loginDto.getEmail(),loginDto.getPassword());
    }


    private UserResponse authenticate(String email, String password) {
        UserResponse userByEmail = getUserByEmail(email);
        try {
            if (userByEmail != null) {
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT));
            }
            Authentication authentication = authenticationManager.authenticate(unauthenticated(email, password));
            UserResponse loggedInUser = getLoggedInUser(authentication);
            assert userByEmail != null;
            loggedInUser.setId(userByEmail.getId());
            loggedInUser.setPhone(userByEmail.getPhone());
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

    private void sendEmail(String firstName, String email, String verificationUrl, VerificationType verificationType) {
        CompletableFuture.runAsync(() -> emailService.sendVerificationEmail(firstName, email, verificationUrl, verificationType));

    }

    @Override
    public UserResponse getUserByEmail(String email) {
        try {
            Users users = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Users", "Email", email));
            return mapper.map(users, UserResponse.class);
        } catch (ResourceNotFoundException exception) {
            // Re-throwing the ResourceNotFoundException with appropriate message
            log.error("User not found: {}", email);
            throw exception; // Make sure this exception is handled by the controller advice
        } catch (Exception exception) {
            // Catch any other generic exceptions
            log.error("An error occurred while fetching the user by email: {}", exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }


    @Override
    public void sendVerificationCode(UserResponse userDto) {
        String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);
        String verificationCode = randomAlphabetic(8).toUpperCase();
        try{
            //DELETE_VERIFICATION_CODE_BY_USER_ID
            twoFactorVerificationsRepository.deleteById(userDto.getId());
            //INSERT_VERIFICATION_CODE_QUERY
            TwoFactorVerifications twoFactorVerifications =new TwoFactorVerifications();
            Users user = mapper.map(userDto,Users.class);
            twoFactorVerifications.setUser(user);
            twoFactorVerifications.setCode(verificationCode);
            twoFactorVerifications.setExpirationDate(DateUtil.parseStringToLocalDateTime(expirationDate));
            twoFactorVerificationsRepository.save(twoFactorVerifications);
            //sendSMS(user.getPhone(), "From: SecureCapita \nVerification code\n" + verificationCode);
            log.info("Verification Code: {}", verificationCode);
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw  new BlogAPIException(HttpStatus.BAD_REQUEST,"An error occurred. Please try again.");
        }

    }

    private void sendSMS(String phone, String s) {
    }

    @Override
    public UserResponse verifyCode(String email, String code) {
        if(isVerificationCodeExpired(code)) throw new ApiException("This code has expired. Please login again.");
        try {
            UserResponse userByCode = mapper.map(userRepository.findByTwoFactorVerifications_Code(code), UserResponse.class);

            userByCode.setRoleName(rolesService.getRoleByUserId(userByCode.getId()).getName());
            userByCode.setPermissions(rolesService.getRoleByUserId(userByCode.getId()).getPermission());

            UserResponse userByEmail = getUserByEmail(email);
            if (userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                // Fetch the verification entity using code
                TwoFactorVerifications verification = twoFactorVerificationsRepository.findByCode(code)
                        .orElseThrow(() -> new ApiException("Verification code not found"));
                // Explicitly dissociate the verification from the user (optional step)
                Users user = verification.getUser();
                user.setTwoFactorVerifications(null);
                userRepository.save(user);  // Save the user to update the relationship
                // Now delete the verification entity
                twoFactorVerificationsRepository.deleteById(verification.getId());
                return userByCode;
            } else {
                throw new ApiException("Invalid code. Please try again.");
            }
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("Could not find record");
        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    private Boolean isVerificationCodeExpired(String code) {
        if (code == null || code.isEmpty()) {
            throw new ApiException("Verification code must not be null or empty");
        }
        try {
            // Fetch the expiration status of the verification code
            Boolean isExpired = twoFactorVerificationsRepository.isCodeExpired(code);
            // Handle the case where the code is not found (null check)
            if (isExpired == null) {
                throw new ApiException("Verification code not found or invalid. Please try again.");
            }
            // Return the expiration status
            return isExpired;
        } catch (ApiException ex) {
            // Custom ApiException, pass it along
            throw ex;
        } catch (Exception exception) {
            // Catch any other unexpected exception and throw a general error
            throw new ApiException("An error occurred. Please try again.");
        }
    }


    @Override
    public void resetPassword(String email) {
        // Normalize and validate the email address
        String normalizedEmail = email.trim().toLowerCase();
        // Check if an account exists for the provided email
        if (getEmailCount(normalizedEmail) <= 0) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "There is no account for this email address.");
        }
        try {
            // Set expiration date to 24 hours from now
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(1);
            // Retrieve the user by email
            UserResponse userDto = getUserByEmail(normalizedEmail);
            // Generate a new verification URL
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
            // Delete any existing password verification entries for this user
            resetPasswordVerificationsRepository.deleteResetPasswordVerificationsByUserId(userDto.getId());
            // Create a new ResetPasswordVerifications entry
            ResetPasswordVerifications resetPasswordVerification = new ResetPasswordVerifications();
            resetPasswordVerification.setUser(mapper.map(userDto, Users.class));
            resetPasswordVerification.setUrl(verificationUrl);
            resetPasswordVerification.setExpirationDate(expirationDate);
            // Save the new reset password verification entry
            resetPasswordVerificationsRepository.save(resetPasswordVerification);
            // Send the verification email
            sendEmail(userDto.getFirstName(), normalizedEmail, verificationUrl, PASSWORD);
            // Log the verification URL for debugging purposes
            log.info("Verification URL sent: {}", verificationUrl);
        } catch (BlogAPIException ex) {
            // Log the specific error message and rethrow the exception
            log.error("Password reset error: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            // Log unexpected errors and throw a generic error message
            log.error("Unexpected error during password reset: {}", ex.getMessage(), ex);
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "An error occurred. Please try again.");
        }
    }


    @Override
    public UserResponse verifyPasswordKey(String key) {
        // Check if the link has expired
        if (isLinkExpired(key, PASSWORD)) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "This link has expired. Please reset your password again.");
        }
        try {
            // Get the verification URL
            String verificationUrl = getVerificationUrl(key, PASSWORD.getType());
            // Find the user by the verification URL and map to UserDto
            Users user = userRepository.findByResetPasswordVerificationUrl(verificationUrl);
            return mapper.map(user, UserResponse.class);
        } catch (BlogAPIException ex) {
            // Re-throw BlogAPIException with the original context
            log.error("Password verification failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            // Log unexpected errors and wrap them in a BlogAPIException
            log.error("Unexpected error during password verification: {}", ex.getMessage(), ex);
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "This link is not valid. Please reset your password again.");
        }
    }


    private Boolean isLinkExpired(String key, VerificationType verificationType) {
        try {
            String verificationUrl = getVerificationUrl(key, verificationType.getType());
            return userRepository.isExpired(verificationUrl);
        } catch (BlogAPIException ex) {
            // Log the specific error message from the BlogAPIException
            log.error("Verification failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            // Log any other unexpected exceptions
            log.error("Unexpected error during verification: {}", ex.getMessage(), ex);
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "This link is not valid. Please reset your password again.");
        }
    }


    @Override
    public void updatePasswordWithKey(String key, String password, String confirmPassword) {
       if(!password.equals(confirmPassword)) throw new BlogAPIException(HttpStatus.BAD_REQUEST,"Passwords don't match. Please try again.");
       try {
           String verificationUrl = getVerificationUrl(key,PASSWORD.getType());
           Users user = userRepository.findByResetPasswordVerificationUrl(verificationUrl);
            //UPDATE_USER_PASSWORD_BY_URL_QUERY
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
           //DELETE_VERIFICATION_BY_URL_QUERY
            resetPasswordVerificationsRepository.deleteResetPasswordVerificationsByUserId(user.getId());
       }catch (Exception exception){
           log.error(exception.getMessage());
           throw new BlogAPIException(HttpStatus.BAD_REQUEST,"An error occurred. Please try again.oo");
       }
    }

    @Override
    public void updatePasswordWithIdUser(Long userId, String password, String confirmPassword) {
        if(!password.equals(confirmPassword)) throw new BlogAPIException(HttpStatus.BAD_REQUEST,"Passwords don't match. Please try again.");
        try {
            Users userInfo=userRepository.getReferenceById(userId);
            //UPDATE_USER_PASSWORD_BY_URL_QUERY
            userInfo.setPassword(passwordEncoder.encode(password));
            userRepository.save(userInfo);
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new BlogAPIException(HttpStatus.BAD_REQUEST,"An error occurred. Please try again.oo");
        }
    }

    @Override
    public UserDto verifyAccountKey(String key) {
        try {
            // Retrieve the verification URL using the provided key and account type
            String verificationUrl = getVerificationUrl(key, ACCOUNT.getType());
            // Find the user associated with the account verification URL
            Users user = userRepository.findByAccountVerificationUrl(verificationUrl);
            // Enable the user's account
            user.setEnabled(true);
            // Save the updated user entity
            Users updatedUser = userRepository.save(user);
            // Map the updated user entity to a UserDto
            return mapper.map(updatedUser, UserDto.class);
        } catch (Exception ex) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "The link is not valid...");
        }
    }


    @Override
    public UserResponse updateUserDetails(UpdateForm updateUser) {
        Users userInfo = mapper.map(updateUser,Users.class);
        Users updatedUser = userRepository.save(userInfo);
        return  mapper.map(updatedUser,UserResponse.class);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        Users user = userRepository.getReferenceById(userId);
        return mapper.map(user,UserResponse.class);
    }

    @Override
    public void updatePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword) {
        // Check if the new password matches the confirmation password
        if (!newPassword.equals(confirmNewPassword)) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Passwords don't match. Please try again.");
        }
        Users user = userRepository.getReferenceById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Incorrect current password. Please try again.");
        }
        try {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } catch (Exception e) {
            // Handle any unexpected exceptions that might occur during the password update
            throw new BlogAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the password. Please try again later.");
        }
    }


    @Override
    public void updateUserRole(Long userId, String roleName) {

    }

    @Override
    public void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked) {
        // Get the instance User
        Users user = userRepository.getReferenceById(userId);
        user.setEnabled(enabled);
        user.setNonLocked(notLocked);
        userRepository.save(user);
    }

    @Override
    public UserResponse toggleMfa(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(
                ()-> new ResourceNotFoundException("Users","Email","email"));
        if(isBlank(user.getPhone())){ throw new BlogAPIException(HttpStatus.BAD_REQUEST,"You need a phone number to change Multi-Factor Authentication");}
         user.setUsingMfa(!user.getUsingMfa());
         try {
              userRepository.save(user);
              return mapper.map(user,UserResponse.class);
         }catch (Exception exception){
             log.error(exception.getMessage());
             throw new BlogAPIException(HttpStatus.BAD_REQUEST,"Unable to update Multi-Factor Authentication");
         }
    }

    @Override
    public void updateImage(UserResponse user, MultipartFile image) {
        String userImageUrl = setUserImageUrl(user.getEmail());
        user.setImageUrl(userImageUrl);
        saveImage(user.getEmail(), image);
        Users updateImageUser = mapper.map(user,Users.class);
        userRepository.save(updateImageUser);
    }



    private void saveImage(String email, MultipartFile image) {
        Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Downloads/images/").toAbsolutePath().normalize();
        if(!Files.exists(fileStorageLocation)) {
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new BlogAPIException(HttpStatus.BAD_REQUEST,"Unable to create directories to save image");
            }
            log.info("Created directories: {}", fileStorageLocation);
        }
        try {
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(email + ".png"), REPLACE_EXISTING);
        } catch (IOException exception) {
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        }
        log.info("File saved in: {} folder", fileStorageLocation);
    }

    private String setUserImageUrl(String email) {
        return fromCurrentContextPath().path("/user/image/" + email + ".png").toUriString();
    }

    private String getVerificationUrl(String key, String type) {
        return fromCurrentContextPath().path("/auth/secureapi/verify/" + type + "/" + key).toUriString();
    }

    private Integer getEmailCount(String email){
        return userRepository.countUsersByEmail(email);
    }


}
