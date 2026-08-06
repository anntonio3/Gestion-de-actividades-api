package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);

    /** Limpieza de tokens ya expirados (se puede llamar con @Scheduled). */
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist t WHERE t.fechaExpiracion < :ahora")
    void eliminarExpirados(LocalDateTime ahora);
}