package com.ecom.config;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.ecom.model.UserDtls;

public class CustomUser implements UserDetails {

	private UserDtls userDtls;
	
	public CustomUser(UserDtls userDtls) {
		super();
		this.userDtls = userDtls;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userDtls.getRole());
		return Arrays.asList(authority);
	}

	@Override
	public String getPassword() {
		return userDtls.getPassword();
	}

	@Override
	public String getUsername() {
		return userDtls.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
	    return true;
	}

	@Override
	public boolean isAccountNonLocked() {
	    return userDtls.getAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
	    return true;
	}

	@Override
	public boolean isEnabled() {
	    return userDtls.getIsEnabled();
	}
}
