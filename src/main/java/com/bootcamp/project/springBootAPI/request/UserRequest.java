package com.bootcamp.project.springBootAPI.request;

import java.util.List;

public class UserRequest {

	private Long id;
	private String name;
	private String username;
	private String email;
	private List<String> roles;

	public UserRequest() {
	}

	public UserRequest(String name, String username, String email) {
		this.name = name;
		this.username = username;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

}
