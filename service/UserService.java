package com.ecom.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.UserDtls;

public interface UserService {
	
	public UserDtls saveUser(UserDtls userDtls);
	public UserDtls save(UserDtls userDtls);
	public UserDtls getUserByEmail(String email);
	public List<UserDtls> getAllUsersByRole(String role);
	public Boolean updateAccountStatus(Integer id, Boolean status);
	public void increaseFailedAttempt(UserDtls userDtls);
	public void userAccountLock(UserDtls userDtls);
	public boolean unlockAccountTimeExpired(UserDtls userDtls);
	public void resetAttempt(int userId);
	public void updateUserResetToken(String email, String resetToken);
	public UserDtls getUserByToken(String token);
	public UserDtls updateUser(UserDtls user);
	public UserDtls updateUserProfile(UserDtls user,MultipartFile img);
	
}
