package org.example.toywebsitebackend.repository;

import org.example.toywebsitebackend.model.Order;
import org.example.toywebsitebackend.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Optional<Order> findByOrderNumberAndUserId(String orderNumber, Long userId);

    List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, LocalDateTime time);
}

