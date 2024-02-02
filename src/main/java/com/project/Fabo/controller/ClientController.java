package com.project.Fabo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.Fabo.entity.Client;
import com.project.Fabo.repository.ClientRepository;
import com.project.Fabo.service.ClientService;

import jakarta.mail.internet.MimeMessage;

@Controller
public class ClientController {
	
	private final ClientRepository clientRepository;
    private final ClientService clientService;


	public ClientController(ClientRepository clientRepository, ClientService clientService) {
		this.clientRepository = clientRepository;
		this.clientService = clientService;
	}
	
	 @Autowired
	 private JavaMailSender javaMailSender;

	@GetMapping("/faboAdd")
	public String faboPage(Model model) {
		Client client = new Client();
		model.addAttribute("client",client);
		return "AddClient";
	}
	
	@RequestMapping(value = {"/faboclients", "/faboclients"}, method = {RequestMethod.GET, RequestMethod.POST})
	public String searchAndFilterClients(@RequestParam(value = "search", required = false) String search,
	                                      @RequestParam(value = "state", required = false) String state,
	                                      @RequestParam(value = "city", required = false) String city,
	                                      Model model) {

	    model.addAttribute("selectedState", state);
	    model.addAttribute("selectedCity", city);

	    // Filter
	    List<String> states = clientRepository.findDistinctStates();
	    List<String> cities = clientRepository.findDistinctCities();
	    model.addAttribute("states", states);
	    model.addAttribute("cities", cities);

	    List<Client> clients;

	    if ((state == null || state.equalsIgnoreCase("All")) && (city == null || city.equalsIgnoreCase("All"))) {
	        // No specific state or city selected, return all active clients
	        clients = clientRepository.findByActiveStatusTrue();
	    } else if (search != null && !search.isEmpty()) {
	        // Search
	        clients = clientRepository.findBySearchTerm(search, state);
	    } else if (state != null && !state.isEmpty() && !state.equalsIgnoreCase("All") && city != null && !city.isEmpty() && !city.equalsIgnoreCase("All")) {
	        // Filter by both state and city
	        clients = clientRepository.findByStateAndCity(state, city);
	    } else if (state != null && !state.isEmpty() && !state.equalsIgnoreCase("All")) {
	        // Filter by state
	        clients = clientRepository.findByState(state);
	    } else if (city != null && !city.isEmpty() && !city.equalsIgnoreCase("All")) {
	        // Filter by city
	        clients = clientRepository.findByCity(city);
	    } else {
	        // Default case, return all active clients
	        clients = clientRepository.findByActiveStatusTrue();
	    }

	    model.addAttribute("clients", clients);
	    return "clientslist";
	}


	
	@PostMapping("/clients")
	public String saveClient(@ModelAttribute("client") Client client) {
	    boolean isUpdate = false;
	     String email = client.getEmail();
	    Optional<Client> existingClientOptional = clientService.getClientByEmail(email);

	    if (existingClientOptional.isPresent()) {
	        // If the client exists, it's an update operation
	        isUpdate = true;
	    } else {
	        // If the client doesn't exist, it's a new entry, so save it
	        clientService.saveClient(client);
	    }

	    sendEmailNotification(client.getEmail(), client, isUpdate);
	    return "redirect:/faboclients";
	}



	
	private void sendEmailNotification(String toEmail, Client client, boolean isUpdate) {
	    try {
	        MimeMessage message = javaMailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, false); // Set to false for plain text

	        String subject = "Confirmation: Your Information is " + (isUpdate ? "Updated" : "Submitted");

	        // Construct email content with user details
	        String emailContent = "Your information has been ";
	        emailContent += (isUpdate ? "updated" : "submitted") + " successfully. Below are the details you provided:\n\n";
	        emailContent += "Store Name: " + client.getStoreName() + "\n";
	        emailContent += "Store Code: " + client.getStoreCode() + "\n";
	        emailContent += "Owner Name: " + client.getOwnerName() + "\n";
	        emailContent += "State: " + client.getState() + "\n";
	        emailContent += "City: " + client.getCity() + "\n";
	        emailContent += "Full Address: " + client.getFullAddress() + "\n";
	        // Add other fields similarly
	        System.out.println("isUpdate value: " + isUpdate);
	        System.out.println("Subject: " + subject);
	        helper.setTo(toEmail);
	        helper.setSubject(subject);
	        helper.setText(emailContent);

	        javaMailSender.send(message); // Send the email
	    } catch (Exception e) {
	        e.printStackTrace(); // Handle email sending exception
	    }
	}

	
	@GetMapping("clients/edit/{Id}")
	public String editClient(@PathVariable Long Id, Model model) {
		model.addAttribute("client", clientService.getClientById(Id));
		return "clientsedit";
	}
	
	@RequestMapping("clientview/{Id}")
	public String viewClient(@PathVariable Long Id, Model model) {
		model.addAttribute("client", clientService.getClientById(Id));
		return "clientsview";
	}

	@PostMapping("clients/{Id}")
	public String updateClient(@PathVariable Long Id,
	                            @ModelAttribute("client") Client updatedClient, 
	                            Model model) {
	    // Retrieve the existing client from the database
	    Client existingClient = clientService.getClientById(Id);

	    if (existingClient != null) {
	        // Update the properties of the existing client
	        existingClient.setStoreCode(updatedClient.getStoreCode());
	        existingClient.setStoreName(updatedClient.getStoreName());
	        existingClient.setOwnerName(updatedClient.getOwnerName());
	        existingClient.setEmail(updatedClient.getEmail());
	        existingClient.setOwnerContact(updatedClient.getOwnerContact());
	        existingClient.setStoreContact(updatedClient.getStoreContact());
	        existingClient.setGstNo(updatedClient.getGstNo());
	        existingClient.setState(updatedClient.getState());
	        existingClient.setCity(updatedClient.getCity());
	        existingClient.setFullAddress(updatedClient.getFullAddress());

	        // Save the updated client
	        clientService.updateClient(existingClient);

	        // Perform other actions as needed
	        sendEmailNotification(existingClient.getEmail(), existingClient, true);
	    }

	    return "redirect:/faboclients";
	}

	
	@GetMapping("/client/{Id}")
	public String deleteClient(@PathVariable Long Id) {
		clientService.deleteClientById(Id);
		return "redirect:/faboclients";
	}
	
	@PostMapping("/handleButtonClick")
    public String handleButtonClick(Model model) {
        return "redirect:/faboclients"; 
    }
	
	 
	 /*@PostMapping("/faboclients")
	 public String searchClients(@RequestParam(value = "search", required = false) String search, Model model) {
	     List<Client> clientss;

	     if (search != null && !search.isEmpty()) {

	         clientss = clientRepository.findBySearchTerm(search);
     } else {

	         clientss = clientRepository.findAll();
	 }

     model.addAttribute("clients", clientss);

     return "faboclients";
	 }*/
	 

}
