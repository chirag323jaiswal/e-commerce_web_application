package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ecom.model.UserDtls;

@Repository
public interface UserRepository extends JpaRepository<UserDtls, Integer> {

	public UserDtls findByEmail(String username);
	public List<UserDtls> findByRole(String role);
	public UserDtls findByResetToken(String token);

}
