package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
//	@Autowired
//	private AuthenticationManager authenticationManager;
	
	@Override
	public UserDtls saveUser(UserDtls userDtls) {
		userDtls.setRole("ROLE_USER");
		userDtls.setIsEnabled(true);
		userDtls.setAccountNonLocked(true);
		userDtls.setFailedAttempt(0);
		String encodedPassword = passwordEncoder.encode(userDtls.getPassword());
		userDtls.setPassword(encodedPassword);
		UserDtls savedUser = userRepository.save(userDtls);
		return savedUser;
	}
	
	@Override
	public UserDtls save(UserDtls userDtls) {
		UserDtls savedUser = userRepository.save(userDtls);
		return savedUser;
	}

	@Override
	public UserDtls getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDtls> getAllUsersByRole(String role) {
		return userRepository.findByRole(role);
	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		Optional<UserDtls> user = userRepository.findById(id);
		if(user.isPresent()) {
			UserDtls userDtls = user.get();
			userDtls.setIsEnabled(status);
			userRepository.save(userDtls);
			return true;
		}
		return false;
	}

	@Override
	public void increaseFailedAttempt(UserDtls userDtls) {
		int attempt = userDtls.getFailedAttempt() + 1;
		userDtls.setFailedAttempt(attempt);
		userRepository.save(userDtls);
	}

	@Override
	public void userAccountLock(UserDtls userDtls) {
		userDtls.setAccountNonLocked(false);
		userDtls.setLockTime(new Date());
		userRepository.save(userDtls);
	}

	@Override
	public boolean unlockAccountTimeExpired(UserDtls userDtls) {
		long lockTime = userDtls.getLockTime().getTime();
		long unLockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
		long currentTime = System.currentTimeMillis();
		if (unLockTime < currentTime) {
			userDtls.setAccountNonLocked(true);
			userDtls.setFailedAttempt(0);
			userDtls.setLockTime(null);
			userRepository.save(userDtls);
			return true;
		}
		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {
		UserDtls findByEmail = userRepository.findByEmail(email);
		findByEmail.setResetToken(resetToken);
		userRepository.save(findByEmail);		
	}

	@Override
	public UserDtls getUserByToken(String token) {
		return userRepository.findByResetToken(token);
	}

	@Override
	public UserDtls updateUser(UserDtls user) {
		return userRepository.save(user);
	}

	@Override
	public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {
		
		UserDtls dbUser = userRepository.findById(user.getId()).get();
		if (!img.isEmpty()) {
			dbUser.setProfileImage(img.getOriginalFilename());
		}
		if (!ObjectUtils.isEmpty(dbUser)) {

			dbUser.setName(user.getName());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			dbUser = userRepository.save(dbUser);
		}
		try {
			if (!img.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ img.getOriginalFilename());
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbUser;
	}

}
