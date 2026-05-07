package com.wms.repository;

import com.wms.entity.InboundOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {
    Optional<InboundOrder> findByOrderNo(String orderNo);

    @Query("SELECT i FROM InboundOrder i LEFT JOIN FETCH i.warehouse LEFT JOIN FETCH i.operator")
    Page<InboundOrder> findAllWithDetails(Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.orderNo, 3) AS integer)), 0) FROM InboundOrder i WHERE i.orderNo LIKE :prefix%")
    Integer findMaxOrderNoByPrefix(@Param("prefix") String prefix);
}
