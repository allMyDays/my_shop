package com.example.user_service.repository;

import com.example.user_service.dto.IdAndKeycloakIdAndAvatarFileName;
import com.example.user_service.entity.MyUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<MyUser, Long> {

   Optional<MyUser> findByKeycloakId(String key);

    List<IdAndKeycloakIdAndAvatarFileName> findByIdIn(List<Long> userEntityIdsList);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO my_user (id, keycloak_id) VALUES (:id, :keycloak_id)", nativeQuery = true)
    void insertUserNativeQuery(@Param("id") Long id,
                  @Param("keycloak_id") String keycloakId);
}




