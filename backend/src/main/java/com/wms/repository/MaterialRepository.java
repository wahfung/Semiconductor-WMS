package com.wms.repository;

import com.wms.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByMaterialCode(String materialCode);
    boolean existsByMaterialCode(String materialCode);

    @Query("SELECT m FROM Material m WHERE m.enabled = true AND " +
           "(m.materialName LIKE %:keyword% OR m.materialCode LIKE %:keyword%)")
    Page<Material> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Material m WHERE m.materialType = :materialType AND m.enabled = true")
    List<Material> findByMaterialType(@Param("materialType") String materialType);

    @Query("SELECT m FROM Material m WHERE m.enabled = true")
    List<Material> findAllEnabled();

    @Query("SELECT m FROM Material m WHERE m.enabled = true")
    Page<Material> findAllEnabled(Pageable pageable);
}
