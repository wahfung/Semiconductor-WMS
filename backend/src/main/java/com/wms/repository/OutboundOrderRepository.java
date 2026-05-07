package com.wms.repository;

import com.wms.entity.OutboundOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, Long> {
    Optional<OutboundOrder> findByOrderNo(String orderNo);

    @Query("SELECT o FROM OutboundOrder o LEFT JOIN FETCH o.warehouse LEFT JOIN FETCH o.operator")
    Page<OutboundOrder> findAllWithDetails(Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(o.orderNo, 3) AS integer)), 0) FROM OutboundOrder o WHERE o.orderNo LIKE :prefix%")
    Integer findMaxOrderNoByPrefix(@Param("prefix") String prefix);
}
