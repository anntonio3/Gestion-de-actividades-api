package mx.edu.unpa.actividadesapi.repository;

import mx.edu.unpa.actividadesapi.model.EspacioEquipamiento;
import mx.edu.unpa.actividadesapi.model.EspacioEquipamientoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EspacioEquipamientoRepository
        extends JpaRepository<EspacioEquipamiento, EspacioEquipamientoId> {

    /**
     * Borra todo el equipamiento de un espacio.
     * Se usa al editar: se reescribe la lista completa para simplificar.
     */
    @Modifying
    @Query("DELETE FROM EspacioEquipamiento e WHERE e.espacio.idRecurso = :idEspacio")
    void eliminarPorEspacio(@Param("idEspacio") Integer idEspacio);
}
