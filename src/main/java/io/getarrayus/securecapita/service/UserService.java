package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.dto.UpdateForm;

import io.getarrayus.securecapita.payload.UserDto;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {

    UserDto createUser(UserDto user);
  // UserResponse login(LoginDto loginDto);
  UserDto getUserByEmail(String email);
    void sendVerificationCode(UserDto user);
  UserDto verifyCode(String email, String code);
    void resetPassword(String email);
  UserDto verifyPasswordKey(String key);
    void updatePasswordWithKey(String key, String password, String confirmPassword);
    void updatePasswordWithIdUser(Long userId, String password, String confirmPassword);
  UserDto verifyAccountKey(String key);
  UserDto updateUserDetails(UpdateForm user);
  UserDto getUserById(Long userId);
    void updatePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword);
    void updateUserRole(Long userId, String roleName);
    void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked);
  UserDto toggleMfa(String email);
    void updateImage(UserDto user, MultipartFile image);


//    String generateRefreshToken(String email);
//    String refreshToken(String refreshToken);
//    UserDto getUserInfoFromToken(String token);

}
