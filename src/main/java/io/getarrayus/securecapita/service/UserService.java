package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.dto.UpdateForm;
import io.getarrayus.securecapita.dto.UserResponse;
import io.getarrayus.securecapita.payload.LoginDto;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserDto user);
    UserResponse login(LoginDto loginDto);
    UserResponse getUserByEmail(String email);
    void sendVerificationCode(UserResponse user);
    UserResponse verifyCode(String email, String code);
    void resetPassword(String email);
    UserResponse verifyPasswordKey(String key);
    void updatePasswordWithKey(String key, String password, String confirmPassword);
    void updatePasswordWithIdUser(Long userId, String password, String confirmPassword);
    UserDto verifyAccountKey(String key);
    UserResponse updateUserDetails(UpdateForm user);
    UserResponse getUserById(Long userId);
    void updatePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword);
    void updateUserRole(Long userId, String roleName);
    void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked);
    UserResponse toggleMfa(String email);
    void updateImage(UserResponse user, MultipartFile image);


//    String generateRefreshToken(String email);
//    String refreshToken(String refreshToken);
//    UserDto getUserInfoFromToken(String token);

}
