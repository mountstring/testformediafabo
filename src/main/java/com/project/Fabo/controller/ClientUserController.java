package com.project.Fabo.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import com.project.Fabo.entity.ClientUser;
import com.project.Fabo.repository.ClientUserRepository;
import com.project.Fabo.service.ClientUserService;
import com.project.Fabo.service.RolesTableService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Controller
public class ClientUserController {
			
	 private static final Logger log = LoggerFactory.getLogger(ClientUserController.class);
	 
	private ClientUserService clientUserService;
	private ClientUserRepository clientUserRepository;
	private RolesTableService rolesTableService;


	public ClientUserController(ClientUserService clientUserService, ClientUserRepository clientUserRepository,
			RolesTableService rolesTableService) {
		this.clientUserService = clientUserService;
		this.clientUserRepository = clientUserRepository;
		this.rolesTableService = rolesTableService;
	}
	
	@Autowired
	private JavaMailSender javaMailSender;

	@PostMapping("/add")
	public String addClientUser(@RequestParam String email,
	                            Model model,
	                            @RequestParam("storeName") String storename,
	                            @RequestParam("storeCode") String storeCode,
	                            @RequestParam List<Long> roleIds,
	                            @ModelAttribute("clientUser") ClientUser clientUser) {

	    String username = clientUser.getUserName();

	    if (clientUserService.isUsernameDuplicate(username)) {
	        // Username is a duplicate, show error
	        model.addAttribute("error", "Username already exists");
	        return "redirect:/faboClientUsers"; // Return to the form with an error message
	    } else {

	    	 // Logic to add ClientUser and associated roles
	        clientUserService.addClientUserAndRoles(clientUser, roleIds);

	        // Save the updated clientUser object (including concatenatedRoleNames)
	        clientUserService.saveUser(clientUser);

	        // Set the concatenatedRoleNames in the clientUser object
	        String concatenatedRoleNames = rolesTableService.getConcatenatedRoleNamesByEmail(email, roleIds);
	        clientUser.setConcatenatedRoleNames(concatenatedRoleNames);
	        clientUserService.updateUser(clientUser);

	        model.addAttribute("email", email);

	        // Send email notification
	        sendEmailNotification(email, clientUser, false);

	        return "redirect:/faboClientUsers";
	    }
	}

 
	 @PostMapping("/ClientUsers")
	 public String saveClientUsers(@ModelAttribute("clientUser") ClientUser clientUser) {
		 boolean isUpdate = false;
	     String email = clientUser.getEmail();
	     Optional<ClientUser> existingClientOptional = clientUserService.getClientUserByEmail(email);
	     if (existingClientOptional.isPresent()) {
		        // If the client exists, it's an update operation
		        isUpdate = true;
		    } else {
		        // If the client doesn't exist, it's a new entry, so save it
		        clientUserService.saveUser(clientUser);
		    }

		    sendEmailNotification(clientUser.getEmail(), clientUser, isUpdate);
		 return "redirect:/faboClientUsers";
	 }
	
	 @RequestMapping(value = {"/faboClientUsers", "/faboClientUsers"}, method = {RequestMethod.GET, RequestMethod.POST})
	 public String accessAndStorecodeFilters(@RequestParam(value = "search", required = false) String search,
	                                          @RequestParam(value = "storeName", required = false) String storeName,
	                                          @RequestParam(value = "storeCode", required = false) String storeCode,
	                                          @RequestParam(value = "accesses", required = false) String accesses,
	                                          Model model) {

	     // Filter
	     List<String> storeCodes = clientUserRepository.findDistinctStoreCodes();
	     List<String> storeNames = clientUserRepository.findDistinctStoreNames();
	     model.addAttribute("storeCodes", storeCodes);
	     model.addAttribute("storeNames", storeNames);

	     // Store filter values in the model
	     model.addAttribute("storeNameParam", storeName);
	     model.addAttribute("storeCodeParam", storeCode);  // Add this line for storeCode

	     List<ClientUser> clientUsers;

	     if (search != null && !search.isEmpty()) {
	         // Search
	         clientUsers = clientUserRepository.findBySearchTerm(search);
	         model.addAttribute("search", search);
	     } else if (storeName != null && !storeName.isEmpty()) {
	         // Filter by store name
	         if ("All".equalsIgnoreCase(storeName)) {
	             // If "All" is selected, get all client users
	             clientUsers = clientUserRepository.findAll();
	         } else {
	             clientUsers = clientUserRepository.findByStoreName(storeName);
	         }
	         model.addAttribute("selectedType", storeName);
	     } else if (storeCode != null && !storeCode.isEmpty()) {
	         // Filter by store code
	         if ("All".equalsIgnoreCase(storeCode)) {
	             // If "All" is selected, get all client users
	             clientUsers = clientUserRepository.findAll();
	         } else {
	             clientUsers = clientUserRepository.findByStoreCode(storeCode);
	         }
	         model.addAttribute("selectedStoreCode", storeCode);
	     } else if (accesses != null && !accesses.isEmpty()) {
	         // Filter by accesses
	         if ("All".equalsIgnoreCase(accesses)) {
	             clientUsers = clientUserRepository.findAll();
	         } else {
	             // Filter by concatenated role names
	             clientUsers = clientUserRepository.findByConcatenatedRoleNamesContaining(accesses);
	         }
	         model.addAttribute("selected", accesses);
	     } else {
	         // No search or filter, return all clients
	         clientUsers = clientUserRepository.findByActiveStatusTrue();
	     }

	     model.addAttribute("clientUsers", clientUsers);
	     return "userslist";
	 }

	//EDIT
	@GetMapping("/clientUsers/edit/{userName}")
	public String editClientUser(@PathVariable String userName, Model model) {
		// Retrieve existing clientUser by username
	    ClientUser existingClientUser = clientUserService.getClientUserById(userName);

	    // Add the existing clientUser to the model
	    model.addAttribute("clientUser", existingClientUser);
	    return "usersedit";
	}
	
	@RequestMapping("/Users/{userName}")
	public String viewClientUser(@PathVariable String userName, Model model) {
		model.addAttribute("clientUser", clientUserService.getClientUserById(userName));
		return "userview";
	}
	
	@PostMapping("/clientUsers/{userName}")
	public String updateClientUser(
	    @PathVariable String userName,
	    String email,
	    @RequestParam(value = "roleIds", required = false, defaultValue = "") List<Long> roleIds,
	    @ModelAttribute("clientUser") ClientUser clientUser,
	    Model model
	) {
		
		log.info("Role IDs in the model: {}", clientUser.getRoleIds());
		// Logic for updating clientUser, roleIds are automatically set by Thymeleaf
		 clientUserService.updateUser(clientUser);
	    // If roleIds is an empty list or null, set it to an empty list
	    roleIds = (roleIds == null || roleIds.isEmpty()) ? Collections.emptyList() : roleIds;

	    // Add new roles and clear previous roles
	    clientUserService.addClientUserAndRoles(clientUser, roleIds);

	    // Retrieve existing client user
	    ClientUser existingClientUser = clientUserService.getClientUserById(userName);

	    // Set updated properties
	    existingClientUser.setStoreCode(clientUser.getStoreCode());
	    existingClientUser.setUserName(clientUser.getUserName());
	    existingClientUser.setEmail(clientUser.getEmail());
	    existingClientUser.setConcatenatedRoleNames("");

	    // Update the concatenatedRoleNames
	    String concatenatedRoleNames = rolesTableService.getConcatenatedRoleNamesByEmail(email, roleIds);
	    clientUserService.updateConcatenatedRolesByEmail(email, concatenatedRoleNames);
	    model.addAttribute("email", email);

	    // Update the user
	    clientUserService.updateUser(existingClientUser);

	    // Send email notification for update
	    sendUpdateEmailNotification(existingClientUser.getEmail(), existingClientUser, true);

	    return "redirect:/faboClientUsers";
	}




	@GetMapping("/clientUser/{userName}")
	public String deleteClientUser(@PathVariable String userName) {
	    clientUserService.removeClientUserAndAssociationsByUserName(userName);
	    clientUserService.deleteClientUserByUserName(userName);
	    return "redirect:/faboClientUsers";
	}
	
	@GetMapping("/Click")
    public String handleButtonClick(Model model) {
        return "redirect:/faboClientUsers"; 
    }
	
	@Async
	public void sendEmailNotification(String toEmail, ClientUser clientUser, boolean isUpdate) {
	    try {
	    	 MimeMessage message = javaMailSender.createMimeMessage();
	         MimeMessageHelper helper = new MimeMessageHelper(message, true); // Set isHtml to true

	        // Construct the password set URL with the user's ID
	        String setPasswordUrl = "http://localhost:8080/password/" + clientUser.getUserName();

	        String subject = "Confirmation: Your Information is " + (isUpdate ? "Updated" : "Submitted");

	        String emailContent = "Your information has been ";
	        emailContent += (isUpdate ? "updated" : "submitted") + " successfully. Below are the details you provided:\n\n";
	        emailContent += "Store Name: " + clientUser.getStoreName() + "\n";
	        emailContent += "Store Code: " + clientUser.getStoreCode() + "\n";
	        emailContent += "User Name: " + clientUser.getUserName() + "\n";
	        emailContent += "Display Name: " + clientUser.getDisplayName() + "\n";
	        emailContent += "City: " + clientUser.getPhoneNumber() + "\n";
	        emailContent += "Email: " + clientUser.getEmail() + "\n";

	        // Retrieve the concatenated role names from the clientUser object
	        String concatenatedRoleNames = clientUser.getConcatenatedRoleNames();
	        emailContent += "Added Roles: " + (concatenatedRoleNames != null ? concatenatedRoleNames : "None") + "\n";
	       
	        String setPasswordLink = "<a href=\"" + setPasswordUrl + "\">Set your password</a>";
	        emailContent += "To set your password, click " + setPasswordLink;
	       
	        log.info("Sending email to {} for user {} with subject: {}", toEmail, clientUser.getUserName(), subject);
	        helper.setTo(toEmail);
	        helper.setSubject(subject);
	        helper.setText(emailContent);

	        javaMailSender.send(message);
	        log.info("Email sent successfully to {} for user {}", toEmail, clientUser.getUserName());
	    } catch (MessagingException | MailException e) {
	        log.error("Error sending email notification to {} for user {}: {}", toEmail, clientUser.getUserName(), e.getMessage(), e);
	        // You may want to handle the error gracefully, such as sending a notification to an admin or retrying later
	    }
	}

	@Async
	public void sendUpdateEmailNotification(String toEmail, ClientUser clientUser, boolean isUpdate) {
	    try {
	        MimeMessage message = javaMailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, false);

	        String subject = "Confirmation: Your Information is " + (isUpdate ? "Updated" : "Submitted");

	        String emailContent = "Your information has been ";
	        emailContent += (isUpdate ? "updated" : "submitted") + " successfully. Below are the details you provided:\n\n";
	        emailContent += "Store Name: " + clientUser.getStoreName() + "\n";
	        emailContent += "Store Code: " + clientUser.getStoreCode() + "\n";
	        emailContent += "User Name: " + clientUser.getUserName() + "\n";
	        emailContent += "Display Name: " + clientUser.getDisplayName() + "\n";
	        emailContent += "City: " + clientUser.getPhoneNumber() + "\n";
	        emailContent += "Email: " + clientUser.getEmail() + "\n";

	        // Retrieve the concatenated role names from the clientUser object
	        String concatenatedRoleNames = clientUser.getConcatenatedRoleNames();
	        emailContent += "Added Roles: " + (concatenatedRoleNames != null ? concatenatedRoleNames : "None") + "\n";

	        log.info("Sending email to {} for user {} with subject: {}", toEmail, clientUser.getUserName(), subject);
	        helper.setTo(toEmail);
	        helper.setSubject(subject);
	        helper.setText(emailContent);

	        javaMailSender.send(message);
	        log.info("Email sent successfully to {} for user {}", toEmail, clientUser.getUserName());
	    } catch (MessagingException | MailException e) {
	        log.error("Error sending email notification to {} for user {}: {}", toEmail, clientUser.getUserName(), e.getMessage(), e);
	        // You may want to handle the error gracefully, such as sending a notification to an admin or retrying later
	    }
	}

	
}