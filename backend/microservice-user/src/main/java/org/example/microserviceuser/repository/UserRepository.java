package org.example.microserviceuser.repository;

import feign.Param;
import org.example.microserviceuser.entity.User;
import org.example.microserviceuser.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email))")
  Optional<User> findByEmailCaseInsensitive(@Param("email") String email);

  @Query("SELECT u FROM User u WHERE TRIM(u.email) = TRIM(:email)")
  Optional<User> findByEmailExact(@Param("email") String email);

  boolean existsByEmail(String email);

  List<User> findByUserRole(UserRole userRole);

  @Query("SELECT u FROM User u WHERE u.userRole IN :roles ORDER BY u.id ASC")
  List<User> findByRoles(@Param("roles") List<UserRole> roles);
}
