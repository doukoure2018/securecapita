package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users,Long> {

    Boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);

    Optional<Users> findById(Long idUser);


    Users findByTwoFactorVerifications_Code(String code);

    Integer countUsersByEmail(String email);
//    Optional<Users> findUsersByAccountVerifications_Url(String url);

    @Query("SELECT u FROM Users u WHERE u.id = (SELECT r.user.id FROM ResetPasswordVerifications r WHERE r.url = :url)")
    Users findByResetPasswordVerificationUrl(@Param("url") String url);

    @Query("SELECT u FROM Users u WHERE u.id = (SELECT r.user.id FROM AccountVerifications r WHERE r.url = :url)")
    Users findByAccountVerificationUrl(@Param("url") String url);

    @Query("SELECT CASE WHEN r.expirationDate < CURRENT_TIMESTAMP THEN true ELSE false END FROM ResetPasswordVerifications r WHERE r.url = :url")
    boolean isExpired(@Param("url") String url);

    @Modifying
    @Transactional
    @Query("UPDATE Users u SET u.firstName = :firstName, u.lastName = :lastName, u.email = :email, u.phone = :phone, u.address = :address, u.title = :title, u.bio = :bio WHERE u.id = :id")
    int updateUserDetails(@Param("firstName") String firstName,
                          @Param("lastName") String lastName,
                          @Param("email") String email,
                          @Param("phone") String phone,
                          @Param("address") String address,
                          @Param("title") String title,
                          @Param("bio") String bio,
                          @Param("id") Long id);



}
