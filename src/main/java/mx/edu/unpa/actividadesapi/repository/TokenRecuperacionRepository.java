package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Integer> {

    Optional<TokenRecuperacion> findByToken(String token);

    // Invalida tokens anteriores del mismo usuario antes de crear uno nuevo
    @Modifying
    @Query("UPDATE TokenRecuperacion t SET t.usado = true WHERE t.usuario.idUsuario = :idUsuario AND t.usado = false")
    void invalidarTokensDeUsuario(@Param("idUsuario") Integer idUsuario);

    @Modifying
    @Query("UPDATE TokenRecuperacion t SET t.usado = true WHERE t.alumno.idAlumno = :idAlumno AND t.usado = false")
    void invalidarTokensDeAlumno(@Param("idAlumno") Integer idAlumno);

    // Limpieza periódica (para uso futuro con @Scheduled)
    @Modifying
    @Query("DELETE FROM TokenRecuperacion t WHERE t.fechaExpiracion < :ahora")
    void eliminarExpirados(@Param("ahora") LocalDateTime ahora);
}
