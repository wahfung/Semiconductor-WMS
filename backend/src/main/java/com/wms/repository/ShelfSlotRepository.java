package com.wms.repository;

import com.wms.entity.ShelfSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShelfSlotRepository extends JpaRepository<ShelfSlot, Long> {
    Optional<ShelfSlot> findBySlotCode(String slotCode);

    @Query("SELECT ss FROM ShelfSlot ss " +
           "LEFT JOIN FETCH ss.material " +
           "LEFT JOIN FETCH ss.shelf s " +
           "LEFT JOIN FETCH s.warehouse " +
           "WHERE ss.shelf.id = :shelfId")
    List<ShelfSlot> findByShelfIdWithMaterial(@Param("shelfId") Long shelfId);

    @Query("SELECT ss FROM ShelfSlot ss WHERE ss.shelf.id = :shelfId AND ss.status = 'EMPTY'")
    List<ShelfSlot> findEmptySlotsByShelfId(@Param("shelfId") Long shelfId);

    @Query("SELECT ss FROM ShelfSlot ss " +
           "LEFT JOIN FETCH ss.material " +
           "LEFT JOIN FETCH ss.shelf s " +
           "LEFT JOIN FETCH s.warehouse " +
           "WHERE s.warehouse.id = :warehouseId")
    List<ShelfSlot> findByWarehouseIdWithDetails(@Param("warehouseId") Long warehouseId);
}
