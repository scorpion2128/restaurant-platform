package io.restaurant.platform.modules.payment.repository;

import io.restaurant.platform.modules.payment.entity.PaymentMethodDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodDetailRepository extends JpaRepository<PaymentMethodDetail, Long> {
}
