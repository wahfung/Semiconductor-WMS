package com.wms.repository;

import com.wms.entity.Shelf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    Optional<Shelf> findByShelfCode(String shelfCode);
    boolean existsByShelfCode(String shelfCode);

    @Query("SELECT s FROM Shelf s LEFT JOIN FETCH s.warehouse WHERE s.warehouse.id = :warehouseId AND s.enabled = true")
    List<Shelf> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT s FROM Shelf s LEFT JOIN FETCH s.warehouse WHERE s.enabled = true")
    List<Shelf> findAllWithWarehouse();

    @Query("SELECT s FROM Shelf s LEFT JOIN FETCH s.warehouse WHERE s.id = :id")
    Optional<Shelf> findByIdWithWarehouse(@Param("id") Long id);

    @Query("SELECT s FROM Shelf s LEFT JOIN FETCH s.warehouse WHERE s.enabled = true")
    Page<Shelf> findAllEnabled(Pageable pageable);
}
