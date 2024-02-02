package com.project.Fabo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.Fabo.entity.Admin;
import com.project.Fabo.entity.Client;
import com.project.Fabo.entity.ClientUser;
import com.project.Fabo.entity.User;
import com.project.Fabo.service.ClientService;
import com.project.Fabo.service.UserService;

@Controller
public class HomeController {
	
@Autowired
private ClientService clientService;

@Autowired
private UserService userService;

public HomeController(ClientService clientService, UserService userService) {
	super();
	this.clientService = clientService;
	this.userService = userService;
}

@Autowired
private BCryptPasswordEncoder passwordEncoder;
	
	 // Default constructor
    public HomeController() {
        // Default constructor body (if needed)
    }
	
    @GetMapping("/superadminHome")    
    public String show(Model model) {
		Client client = new Client();
		model.addAttribute("client",client);
		ClientUser clientUser = new ClientUser();
		model.addAttribute("clientUser",clientUser);
		Admin admin = new Admin();
		model.addAttribute("admin",admin);
		 List<Client> clients = clientService.getAllClients();
	        model.addAttribute("clients", clients);
		return "superhome"; 
    }

	    @GetMapping("/adminshome")
	    public String adminHome() {
	        // Logic for admin functionalities
	        return "adminshome"; // Assuming there's a corresponding HTML file
	    }

	    @GetMapping("/clientshome")
	    public String clientHome() {
	        // Logic for client functionalities
	        return "clientshome"; // Assuming there's a corresponding HTML file
	    }
	    
	    @GetMapping("/password/{userName}")
	    public String generatePassword(Model model, @PathVariable String userName) {
	    	User user = userService.getUserByUserName(userName);
	    	model.addAttribute("user",user);
	    	model.addAttribute("userName",userName);
	    	return "set";
	    }
	    
	    @PostMapping("/savePassword/{userName}")
	    public String savePassword(Model model,
	    		@RequestParam("userName") String userName, 
	    		@RequestParam("newPassword") String newPassword, 
                @RequestParam("confirmPassword") String confirmPassword) {
	        User existingUser = userService.getUserByUserName(userName);
	        model.addAttribute("existingUser", existingUser);
	        
	        // Hash the password using BCrypt
	        String hashedPassword = passwordEncoder.encode(newPassword);
	        
	        existingUser.setPassword(hashedPassword);
	        
	        // Save the updated user with hashed password
	        userService.saveUser(existingUser);
	        
	        model.addAttribute("successMessage", "Password has been set successfully.");
	        
	        return "redirect:/showLoginPage?success=true";
	    }
	    
	/*    private User getCurrentUser() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        if (authentication != null) {
	            Object principal = authentication.getPrincipal();
	            if (principal instanceof User) {
	                return (User) principal;
	            } else {
	                // Handle case where principal is not an instance of your User entity
	                throw new IllegalStateException("Unexpected user principal type");
	            }
	        } else {
	            // Handle case where no user is authenticated
	            throw new IllegalStateException("No authenticated user found");
	        }
	    }     

	    
	    @GetMapping("/password")
	    public String Password(Model model) {
	    
	    	return "reset";
	    }
	    
	    
	    @PostMapping("/changePassword")
	    public String changePassword(Model model, @RequestParam("oldPassword") String oldPassword,
	                                 @RequestParam("newPassword") String newPassword,
	                                 @RequestParam("confirmPassword") String confirmPassword) {
	        // Get the currently logged-in user (you need to implement this method)
	        User currentUser = getCurrentUser();
	        
	        // Retrieve the existing password associated with the user
	        String existingPassword = currentUser.getPassword();
	        
	        // Check if the entered old password matches the existing password
	        if (!passwordEncoder.matches(oldPassword, existingPassword)) {
	            // If the passwords don't match, display an error message
	            model.addAttribute("error", "Old password is incorrect.");
	            return "reset"; // Return the view with the error message
	        }
	        
	        // Proceed with changing the password if old password is correct
	        if (!newPassword.equals(confirmPassword)) {
	            // If the new password and confirm password don't match, display an error message
	            model.addAttribute("error", "New password and confirm password do not match.");
	            return "reset"; // Return the view with the error message
	        }
	        
	        // Update the user's password with the new password
	        currentUser.setPassword(passwordEncoder.encode(newPassword));
	        userService.saveUser(currentUser);
	        
	        // Redirect the user to a success page or home page
	        return "redirect:/showLoginPage"; // Redirect to home page after successful password change
	    }
*/
}
