package com.example.devso.repository;

import com.example.devso.entity.AuthProvider;
import com.example.devso.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {


    @Query(value = "SELECT * FROM users WHERE username = :username AND deleted_at IS NULL", nativeQuery = true)
    Optional<User> findByUsername(@Param("username") String username);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM users WHERE username = :username AND deleted_at IS NULL", nativeQuery = true)
    long existsByUsername(@Param("username") String username);


    @Query(value = "SELECT * FROM users WHERE provider = :provider AND provider_id = :providerId AND deleted_at IS NULL", nativeQuery = true)
    Optional<User> findByProviderAndProviderId(@Param("provider") AuthProvider provider, @Param("providerId") String providerId);

    @Query("SELECT u FROM User u WHERE (u.username LIKE %:query% OR u.name LIKE %:query%) AND u.id <> :excludeUserId AND u.deletedAt IS NULL")
    List<User> searchUsers(@Param("query") String query, @Param("excludeUserId") Long excludeUserId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.careers " +
            "LEFT JOIN FETCH u.educations " +
            "LEFT JOIN FETCH u.certis " +
            "LEFT JOIN FETCH u.activities " +
            "LEFT JOIN FETCH u.skills " +
            "WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findUserWithDetailsByUsername(@Param("username") String username);

    boolean existsByEmail(String email);
}

