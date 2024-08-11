package org.example.repository;

import org.apache.catalina.User;
import org.example.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Client, UUID> {
    @Query("SELECT u FROM Client u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
}
