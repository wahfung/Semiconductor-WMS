package com.wms.repository;

import com.wms.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT i FROM Inventory i " +
           "LEFT JOIN FETCH i.material " +
           "LEFT JOIN FETCH i.warehouse " +
           "LEFT JOIN FETCH i.shelf " +
           "LEFT JOIN FETCH i.slot " +
           "WHERE i.warehouse.id = :warehouseId")
    List<Inventory> findByWarehouseIdWithDetails(@Param("warehouseId") Long warehouseId);

    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.material WHERE i.material.id = :materialId")
    List<Inventory> findByMaterialId(@Param("materialId") Long materialId);

    @Query(value = "SELECT i FROM Inventory i LEFT JOIN FETCH i.material LEFT JOIN FETCH i.warehouse",
           countQuery = "SELECT COUNT(i) FROM Inventory i")
    Page<Inventory> findAllWithDetails(Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.material.id = :materialId")
    Integer getTotalQuantityByMaterialId(@Param("materialId") Long materialId);

    @Query("SELECT i FROM Inventory i " +
           "LEFT JOIN FETCH i.material " +
           "LEFT JOIN FETCH i.warehouse " +
           "LEFT JOIN FETCH i.shelf " +
           "LEFT JOIN FETCH i.slot " +
           "WHERE i.slot.id = :slotId")
    List<Inventory> findBySlotId(@Param("slotId") Long slotId);
}
