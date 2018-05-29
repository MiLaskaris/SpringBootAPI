package com.bootcamp.project.springBootAPI.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootcamp.project.springBootAPI.model.Message;
import com.bootcamp.project.springBootAPI.model.User;
import com.bootcamp.project.springBootAPI.repository.MessageRepository;
import com.bootcamp.project.springBootAPI.repository.UserRepository;
import com.bootcamp.project.springBootAPI.request.MessageRequest;
import com.bootcamp.project.springBootAPI.response.ApiResponse;
import com.bootcamp.project.springBootAPI.security.CurrentUser;
import com.bootcamp.project.springBootAPI.security.UserPrincipal;
import com.bootcamp.project.springBootAPI.services.MessageServices;

@RestController
@RequestMapping("/api/message")
public class MessageController {

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private MessageServices messageServices;

	@Autowired
	UserRepository userRepository;

	private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

	@PostMapping(path = "/send", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> sendMessage(@CurrentUser UserPrincipal currentUser,
			@RequestBody MessageRequest messageRequest) {
		return messageServices.sendMessage(currentUser, messageRequest);

	}

	@GetMapping(path = "/sentmessages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('USER')")
	public List<MessageRequest> getSent(@CurrentUser UserPrincipal currentUser) {
				return messageServices.getSent(currentUser);
			}

	@GetMapping(path = "/sentmessages/{username}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('ADMIN') or hasRole('GOD')")
	public List<MessageRequest> getSentMsgByUsername(@CurrentUser UserPrincipal currentUser,
			@PathVariable("username") String username) {
		
			return messageServices.getSentMsgByUsername(username);
			}

	@GetMapping(path = "/receivedmessages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('USER')")
	public List<MessageRequest> getReceived(@CurrentUser UserPrincipal currentUser) {

		return messageServices.getReceived(currentUser);

	}

	@GetMapping(path = "/receivedmessages/{username}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('ADMIN') or hasRole('GOD')")
	public List<MessageRequest> getReceivedMsgByUsername(@CurrentUser UserPrincipal currentUser,
			@PathVariable("username") String username) {

		Optional<User> receiver = userRepository.findByUsernameIgnoreCase(username);
		try {

			Set<Message> messages = receiver.get().getReceivedMessage();

			List<MessageRequest> messagesRequest = messages.stream().map(m -> {
				MessageRequest messageRequest = new MessageRequest();
				messageRequest.setId(m.getId());
				messageRequest.setMessage(m.getMessage());
				messageRequest.setSender(m.getSender().getUsername());
				messageRequest.setReceiver(m.getReceiver().getUsername());
				return messageRequest;
			}).collect(Collectors.toList());

			return messagesRequest;

		} catch (Exception e) {
			return Collections.emptyList();
		}

	}

	@GetMapping(path = "/allmessages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('USER')")
	public List<MessageRequest> getMessageList(@CurrentUser UserPrincipal currentUser) {

		Optional<User> user = userRepository.findById(currentUser.getId());
		try {

			Set<Message> sentmessages = user.get().getSentMessage();
			Set<Message> receivedmessages = user.get().getReceivedMessage();

			Set<Message> messages = sentmessages.stream().collect(Collectors.toSet());
			messages.addAll(receivedmessages);

			List<MessageRequest> messagesRequest = messages.stream().map(m -> {
				MessageRequest messageRequest = new MessageRequest();
				messageRequest.setId(m.getId());
				messageRequest.setMessage(m.getMessage());
				messageRequest.setSender(m.getSender().getUsername());
				messageRequest.setReceiver(m.getReceiver().getUsername());
				return messageRequest;
			}).collect(Collectors.toList());

			return messagesRequest;

		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@PostMapping(path = "/deletemessage/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('GOD') or hasRole('ADMIN')")
	public ResponseEntity<?> deletemessage(@CurrentUser UserPrincipal currentUser, @PathVariable("id") Long id) {

		try {
			messageRepository.deleteById(id);
		} catch (Exception e) {
			return new ResponseEntity(new ApiResponse(false, "Invalid operation"), HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok(new ApiResponse(true, "Message was deleted successfully"));
	}

	@PostMapping(path = "/updatemessage", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('GOD') or hasRole('ADMIN')")
	public ResponseEntity<?> updatemessage(@CurrentUser UserPrincipal currentUser,
			@RequestBody MessageRequest messageRequest) {

		Optional<User> sender = userRepository.findByUsernameIgnoreCase(messageRequest.getSender());
		Optional<User> receiver = userRepository.findByUsernameIgnoreCase(messageRequest.getReceiver());

		try {
			User send = sender.get();
			User receive = receiver.get();
			Message message = new Message(messageRequest.getId(), messageRequest.getMessage(), send, receive);

			messageRepository.save(message);

		} catch (Exception e) {

			return new ResponseEntity(new ApiResponse(false, "Invalid request"), HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok(new ApiResponse(true, "Message updated successfully"));

	}

}
