package com.ecom.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.CartService;

@Service
public class CartServiceImpl implements CartService {
	
	@Autowired
	CartRepository cartRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ProductRepository productRepository;
	
	@Override
	public Cart saveCart(Integer productId, Integer userId) {
		UserDtls userDtls = userRepository.findById(userId).get();
		Product product = productRepository.findById(productId).get();
		
		Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);
		Cart cart = null;
		
		if(ObjectUtils.isEmpty(cartStatus)) {
			cart = new Cart();
			cart.setProduct(product);
			cart.setUser(userDtls);
			cart.setQuantity(1);
			cart.setTotalPrice(1 * product.getDiscountedPrice());
		}else {
			cart = cartStatus;
			cart.setQuantity(cart.getQuantity() + 1);
			cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountedPrice());
		}
		Cart savedCart = cartRepository.save(cart);
		return savedCart;
	}

	@Override
	public List<Cart> getCartsByUserId(Integer userId) {
		List<Cart> carts = cartRepository.findByUserId(userId);
		List<Cart> updatedCarts = new ArrayList<>();
		Double totalPrice;
		Double totalOrderPrice=0.0;
		for(Cart c:carts)
		{
			totalPrice = (c.getProduct().getDiscountedPrice() * c.getQuantity());
			c.setTotalPrice(totalPrice);
			totalOrderPrice = totalOrderPrice + totalPrice;
			c.setTotalOrderPrice(totalOrderPrice);
			updatedCarts.add(c);
		}
		return updatedCarts;
	}

	@Override
	public Integer getCountCart(Integer userId) {
		Integer countByUserId = cartRepository.countByUserId(userId);
		return countByUserId;
	}

	@Override
	public void updateQuantity(String sign, Integer cartId) {

		Cart cart = cartRepository.findById(cartId).get();
		int updatedQuantity;

		if (sign.equalsIgnoreCase("de")) {
			updatedQuantity = cart.getQuantity() - 1;

			if (updatedQuantity <= 0) {
				cartRepository.delete(cart);
			} else {
				cart.setQuantity(updatedQuantity);
				cartRepository.save(cart);
			}

		} else {
			updatedQuantity = cart.getQuantity() + 1;
			cart.setQuantity(updatedQuantity);
			cartRepository.save(cart);
		}

	}
	
}
