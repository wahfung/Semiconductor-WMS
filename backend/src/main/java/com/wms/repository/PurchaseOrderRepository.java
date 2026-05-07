package com.wms.repository;

import com.wms.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByOrderNo(String orderNo);

    @Query("SELECT p FROM PurchaseOrder p LEFT JOIN FETCH p.creator WHERE p.status = :status")
    Page<PurchaseOrder> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM PurchaseOrder p LEFT JOIN FETCH p.creator LEFT JOIN FETCH p.items")
    Page<PurchaseOrder> findAllWithDetails(Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.orderNo, 3) AS integer)), 0) FROM PurchaseOrder p WHERE p.orderNo LIKE :prefix%")
    Integer findMaxOrderNoByPrefix(@Param("prefix") String prefix);
}
