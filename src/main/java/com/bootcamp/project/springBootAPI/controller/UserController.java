package com.bootcamp.project.springBootAPI.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootcamp.project.springBootAPI.exceptions.AppException;
import com.bootcamp.project.springBootAPI.model.Role;
import com.bootcamp.project.springBootAPI.model.User;
import com.bootcamp.project.springBootAPI.repository.RoleRepository;
import com.bootcamp.project.springBootAPI.repository.UserRepository;
import com.bootcamp.project.springBootAPI.request.UserRequest;
import com.bootcamp.project.springBootAPI.request.UserRoleRequest;
import com.bootcamp.project.springBootAPI.security.CurrentUser;
import com.bootcamp.project.springBootAPI.security.UserPrincipal;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('USER')")
	public List<String> getUsers(@CurrentUser UserPrincipal currentUser) {
		Collection<User> users = (Collection<User>) userRepository.findAll();

		return users.stream().map(u -> {
			return u.getUsername();
		}).collect(Collectors.toList());

	}

	@GetMapping(path = "/about/me", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('USER')")
	public UserRequest getUserInfo(@CurrentUser UserPrincipal currentUser) {
		UserRequest user = new UserRequest();
		user.setName(currentUser.getName());
		user.setUsername(currentUser.getUsername());
		user.setEmail(currentUser.getEmail());
		user.setRoles(currentUser.getAuthorities().stream().map(r ->{return r.getAuthority();}).collect(Collectors.toList()));
			

		return user;}

	@GetMapping(path = "/rolelist", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('ADMIN') or hasRole('GOD')")
	public List<UserRequest> getUserList(@CurrentUser UserPrincipal currentUser) {

		Collection<User> users = (Collection<User>) userRepository.findAll();

		List<UserRequest> userRequests = users.stream().map(u -> {
			UserRequest userRequest = new UserRequest();
			userRequest.setId(u.getId());
			userRequest.setName(u.getName());
			userRequest.setUsername(u.getUsername());
			userRequest.setEmail(u.getEmail());

			userRequest.setRoles(u.getRoles().stream().map(r -> {
				return r.getName().name();
			}).collect(Collectors.toList()));

			return userRequest;
		}).collect(Collectors.toList());

		return userRequests;

	}

	@PostMapping(path = "/addrole", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('ADMIN') or hasRole('GOD')")
	public void addRole(@CurrentUser UserPrincipal currentUser, @RequestBody UserRoleRequest roleRequest) {
		if (roleRequest != null && roleRequest.getRoleId() > 0 && roleRequest.getUserId() > 0) {
			User user = userRepository.findById(roleRequest.getUserId())
					.orElseThrow(() -> new AppException("User not Found !"));

			Role role = roleRepository.findById(roleRequest.getRoleId())
					.orElseThrow(() -> new AppException("Role not Found !"));
			user.getRoles().add(role);

			userRepository.save(user);
		} else {
			throw new AppException("Wrong body");
		}
	}

	@PostMapping(path = "/deleterole", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('ADMIN') or hasRole('GOD')")
	public void deleteRole(@CurrentUser UserPrincipal currentUser, @RequestBody UserRoleRequest roleRequest) {
		if (roleRequest != null && roleRequest.getRoleId() > 0 && roleRequest.getUserId() > 0) {
			User user = userRepository.findById(roleRequest.getUserId())
					.orElseThrow(() -> new AppException("User not Found !"));

			Role role = roleRepository.findById(roleRequest.getRoleId())
					.orElseThrow(() -> new AppException("Role not Found !"));
			user.getRoles().remove(role);

			userRepository.save(user);
		} else {
			throw new AppException("Wrong body");
		}
	}

}
