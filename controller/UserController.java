package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.mail.Session;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	CategoryService categoryService;
	
	@Autowired
	CartService cartService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void getUserDetails(Principal principal, Model m) {
		if(principal != null) {
			String email = principal.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categories", allActiveCategory);
	}
	
	@GetMapping("/")
	public String home() {
		return "user/home";
	}
	
	@GetMapping("/addToCart")
	public String addToCart(@RequestParam Integer productId, @RequestParam Integer userId, HttpSession session) {
		Cart savedCart = cartService.saveCart(productId, userId);
		if(ObjectUtils.isEmpty(savedCart)) {
			session.setAttribute("errorMsg", "Product add to cart failed");
		}else {
			session.setAttribute("succMsg", "Product added to cart");
		}
		return "redirect:/product/"+ productId;
	}
	
	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m)
	{
		UserDtls userDtls = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUserId(userDtls.getId());
		Double totalOrderPrice = carts.getLast().getTotalOrderPrice();
		m.addAttribute("totalOrderPrice", totalOrderPrice);
		m.addAttribute("carts",carts);
		return "/user/cart";
	}
	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
	
	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sign, @RequestParam Integer cartId) {
		cartService.updateQuantity(sign, cartId);
		return "redirect:/user/cart";
	}
	
	@GetMapping("/orders")
	public String orderPage(Principal p, Model m) {
		UserDtls userDtls = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUserId(userDtls.getId());
		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice() + 250 + 100;
			m.addAttribute("orderPrice", orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/order";
	}
	
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p) {
		// System.out.println(request);
		UserDtls user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), request);

		return "redirect:/user/success";
	}
	
	@GetMapping("/success")
	public String loadSuccess() {
		return "/user/success";
	}
	
	@GetMapping("/user-orders")
	public String myOrder(Model m, Principal p) {
		UserDtls loginUser = getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
		m.addAttribute("orders", orders);
		return "/user/orders";
	}
	
	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;
		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}
		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status updated");
		} else {
			session.setAttribute("errorMsg", "Status not updated");
		}
		return "redirect:/user/user-orders";
	}
	@GetMapping("/profile")
	public String profile()
	{
		return "/user/profile";
	}
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile updated");
		}
		return "redirect:/user/profile";
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);
		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not updated !! error in server");
			} else {
				session.setAttribute("succMsg", "Password updated sucessfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password incorrect");
		}
		return "redirect:/user/profile";
	}
	
}
