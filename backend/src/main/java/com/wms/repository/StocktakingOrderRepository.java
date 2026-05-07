package com.wms.repository;

import com.wms.entity.StocktakingOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StocktakingOrderRepository extends JpaRepository<StocktakingOrder, Long> {
    Optional<StocktakingOrder> findByOrderNo(String orderNo);

    @Query("SELECT s FROM StocktakingOrder s LEFT JOIN FETCH s.warehouse LEFT JOIN FETCH s.operator")
    Page<StocktakingOrder> findAllWithDetails(Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.orderNo, 3) AS integer)), 0) FROM StocktakingOrder s WHERE s.orderNo LIKE :prefix%")
    Integer findMaxOrderNoByPrefix(@Param("prefix") String prefix);
}
