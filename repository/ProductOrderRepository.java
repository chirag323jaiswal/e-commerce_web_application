package com.ecom.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ecom.model.ProductOrder;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {

	public List<ProductOrder> findByUserId(Integer userId);

	public ProductOrder findByOrderId(String orderId);

}