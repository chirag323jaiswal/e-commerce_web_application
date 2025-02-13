package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private ProductService productService;
	@Autowired
	UserService userService;
	@Autowired
	CartService cartService;
	@Autowired
	OrderService orderService;
	@Autowired
	CommonUtil commonUtil;
	
	@ModelAttribute
	public void getUserDetails(Principal principal, Model m) {
		if(principal != null) {
			String email = principal.getName();
			UserDtls userDtls=null;
			userDtls = userService.getUserByEmail(email);
			userDtls.setFailedAttempt(0);
			userDtls = userService.save(userDtls);			
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categories", allActiveCategory);
	}
	
	@GetMapping("/")
	public String index() {
		return "admin/index";
	}
	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
	}
	@GetMapping("/category")
	public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		// m.addAttribute("categories", categoryService.getAllCategory());
		Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
		List<Category> categories = page.getContent();
		m.addAttribute("categories", categories);
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "admin/category";
	}
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category,@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		String imageName = file!=null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);
		if(categoryService.existsCategory(category.getName())) {
			session.setAttribute("errorMsg", "Category name already exists");
		}
		else {
			Category category2=categoryService.saveCategory(category);
			if(ObjectUtils.isEmpty(category2)) {
				session.setAttribute("errorMsg", "Not saved | internal server error");
			}
			else {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" +
							File.separator + file.getOriginalFilename());
				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("succMsg", "Saved successfully");
			}
		}
		return "redirect:/admin/category";
	}
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);
		if(deleteCategory) {
			session.setAttribute("succMsg", "category deleted successfully");
		}
		else {
			session.setAttribute("errorMsg", "something went wrong on server");
		}
		return "redirect:/admin/category";
	}
	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}
	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		Category oldCategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
		if(!ObjectUtils.isEmpty(category)) {
			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}
		Category updatedCategory = categoryService.saveCategory(oldCategory);
		if(!ObjectUtils.isEmpty(updatedCategory)) {
			if(!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" +
							File.separator + file.getOriginalFilename());
				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("succMsg", "Category updated successfully");
		}
		else {
			session.setAttribute("errorMsg", "something went wrong on server");
		}
		return "redirect:/admin/loadEditCategory/" + category.getId();
	}
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		product.setImage(imageName);
		product.setDiscount(0);
		product.setDiscountedPrice(product.getPrice());
		Product savedProduct = productService.saveProduct(product);
		if(!ObjectUtils.isEmpty(savedProduct)) {
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator +
						file.getOriginalFilename());
			System.out.println(path);
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			session.setAttribute("succMsg", "Product saved successfully");
		}
		else {
			session.setAttribute("errorMsg", "Something went wrong on server");
		}
		return "redirect:/admin/loadAddProduct";
	}
	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

//		List<Product> products = null;
//		if (ch != null && ch.length() > 0) {
//			products = productService.searchProduct(ch);
//		} else {
//			products = productService.getAllProducts();
//		}
//		m.addAttribute("products", products);

		Page<Product> page = null;
		if (ch != null && ch.length() > 0) {
			page = productService.searchProductPagination(pageNo, pageSize, ch);
		} else {
			page = productService.getAllProductsPagination(pageNo, pageSize);
		}
		m.addAttribute("products", page.getContent());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/products";
	}
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			session.setAttribute("succMsg", "Product deleted successfully");
		} else {
			session.setAttribute("errorMsg", "Something went wrong on server");
		}
		return "redirect:/admin/products";
	}
	@GetMapping("/loadEditProduct/{id}")
	public String loadEditProduct(@PathVariable int id,Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_product";
	}
	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
			HttpSession session) {
		if(product.getDiscount() < 0 || product.getDiscount() > 100) {
			session.setAttribute("errorMsg", "Invalid discount");
		}
		else {
			Product updatedProduct = productService.updateProduct(product, file);
			if (!ObjectUtils.isEmpty(updatedProduct)) {
				session.setAttribute("succMsg", "Product updated successfully");
			} else {
				session.setAttribute("errorMsg", "Something went wrong on server");
			}
		}
		return "redirect:/admin/loadEditProduct/" + product.getId();
	}
	@GetMapping("/users")
	public String getAllUsers(Model m) 
	{
		List<UserDtls> users = userService.getAllUsersByRole("ROLE_USER");
		m.addAttribute("users", users);
		return "/admin/users";	
	}
	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, HttpSession session) {
		Boolean updatedStatus = userService.updateAccountStatus(id, status);
		if(updatedStatus) {
			session.setAttribute("succMsg", "Account status updated");
		}else {
			session.setAttribute("errorMsg", "Something went wrong on server");
		}
		return "redirect:/admin/users";
	}
	@GetMapping("/orders")
	public String getAllOrders(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
//		List<ProductOrder> allOrders = orderService.getAllOrders();
//		m.addAttribute("orders", allOrders);
//		m.addAttribute("srch", false);

		Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
		m.addAttribute("orders", page.getContent());
		m.addAttribute("srch", false);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "/admin/orders";
	}
	@PostMapping("/update-order-status")
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
		return "redirect:/admin/orders";
	}
	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session) {

		if (orderId != null && orderId.length() > 0) {
			ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());
			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect orderId");
				m.addAttribute("orderDtls", null);
			} else {
				m.addAttribute("orderDtls", order);
			}
			m.addAttribute("srch", true);
		} else {
			List<ProductOrder> allOrders = orderService.getAllOrders();
			m.addAttribute("orders", allOrders);
			m.addAttribute("srch", false);
		}
		return "/admin/orders";
	}

}
