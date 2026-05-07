package com.wms.repository;

import com.wms.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByWarehouseCode(String warehouseCode);
    boolean existsByWarehouseCode(String warehouseCode);

    @Query("SELECT w FROM Warehouse w WHERE w.enabled = true")
    List<Warehouse> findAllEnabled();

    @Query("SELECT w FROM Warehouse w WHERE w.enabled = true")
    Page<Warehouse> findAllEnabled(Pageable pageable);
}
