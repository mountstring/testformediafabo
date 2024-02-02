package com.project.Fabo.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.Fabo.entity.AddSupportAdmin;
import com.project.Fabo.entity.AdminComments;
import com.project.Fabo.entity.Client;
import com.project.Fabo.entity.ClientSupport;
import com.project.Fabo.entity.SupportFiles;
import com.project.Fabo.repository.ClientRepository;
import com.project.Fabo.repository.ClientSupportRepository;
import com.project.Fabo.service.AddSupportAdminService;
import com.project.Fabo.service.AdminCommentService;
import com.project.Fabo.service.ClientSupportService;
import com.project.Fabo.service.SupportMirrorService;
import com.project.Fabo.service.UserService;

@Controller
public class ClientSupportController {
	
	private ClientSupportService clientSupportService;
	
	private ClientRepository clientRepository;
	
	private ClientSupportRepository clientSupportRepository;
	
	private SupportMirrorService supportMirrorService;
	
	private AddSupportAdminService addSupportAdminService;
	
	private AdminCommentService admincommentService;
	
	private UserService userService;

	@GetMapping("/clientsupport")
	public String createSupportByClient(Model model) { 
		List<Client> clients;
		clients = clientRepository.findAll();
		 ClientSupport clientSupport = new ClientSupport();
		model.addAttribute("clientSupport",clientSupport);
		 model.addAttribute("clients", clients);
		return "clientaddsupport"; 
	}
	
	public ClientSupportController(ClientSupportService clientSupportService, ClientRepository clientRepository,
			ClientSupportRepository clientSupportRepository, SupportMirrorService supportMirrorService,
			AddSupportAdminService addSupportAdminService, AdminCommentService admincommentService,
			UserService userService, List<String> base64ImageList) {
		super();
		this.clientSupportService = clientSupportService;
		this.clientRepository = clientRepository;
		this.clientSupportRepository = clientSupportRepository;
		this.supportMirrorService = supportMirrorService;
		this.addSupportAdminService = addSupportAdminService;
		this.admincommentService = admincommentService;
		this.userService = userService;
		this.base64ImageList = base64ImageList;
	}

	@PostMapping("/clientsupportview")
	public String saveClientSupport(@ModelAttribute("clientSupport") ClientSupport clientSupport,
			@RequestParam("storeName") String storeName,
			 @RequestParam(value = "files", required = false)  List<MultipartFile> files) {
		clientSupport.setStatus("New");
		clientSupport.setCommentsToAdmin("No Comments");
		clientSupport.setExternalComments("No Comments");
		
		clientSupportService.saveClientSupport(clientSupport);
		  supportMirrorService.addSupportRecordToBothSide(clientSupport);
		  AddSupportAdmin addSupportAdmin = addSupportAdminService.getAddSupportAdminById(clientSupport.getId());
		 List<SupportFiles> adminSupportFiles = new ArrayList<>();

	        if (files != null && !files.isEmpty()) {
	            for (MultipartFile file : files) {
	                try {
	                	SupportFiles adminSupportFile = new SupportFiles();
	                	adminSupportFile.setFileData(file.getBytes());
	                	adminSupportFile.setAddSupportAdmin(addSupportAdmin);
	                	adminSupportFile.setClientSupport(clientSupport);
	                	adminSupportFiles.add(adminSupportFile);
	                } catch (IOException e) {
	                    // Handle the IOException as needed
	                    e.printStackTrace(); // Log the exception or throw a custom exception
	                }
	            }
	        }

	        // Associate files with the invoice
	        clientSupport.setAdminSupportFiles(adminSupportFiles);
		clientSupportService.saveClientSupport(clientSupport);
		
		
		return "redirect:/viewclientsupport";
	}
	
	/*@GetMapping("/viewclientsupport")
	public String filtersWithList(
	    @RequestParam(value = "supportReqyestType", required = false) String supportRequesttype,
	    @RequestParam(value = "statusDropdown", required = false) String statusDropdown,
	    @RequestParam(value = "searchTerm", required = false) String searchTerm,
	    Model model
	) {
	    List<ClientSupport> clientSupports;

	    if (searchTerm != null && !searchTerm.isEmpty()) {
	        // Filter by search term across multiple fields
	        clientSupports = clientSupportRepository.findBySearchTerm(searchTerm);
	        model.addAttribute("searchTerm", searchTerm);
	    } else if (supportRequesttype != null && !supportRequesttype.isEmpty()) {
	        // Filter by invoice dropdown
	        if ("All".equalsIgnoreCase(supportRequesttype)) {
	            clientSupports = clientSupportRepository.findByActiveStatusTrue();
	        } else {
	            clientSupports = clientSupportRepository.findBySupportRequestTypeContaining(supportRequesttype);
	        }
	        model.addAttribute("selectedType", supportRequesttype);
	    } else if (statusDropdown != null && !statusDropdown.isEmpty()) {
	        // Filter by status dropdown
	        if ("All".equalsIgnoreCase(statusDropdown)) {
	            clientSupports = clientSupportRepository.findByActiveStatusTrue();
	        } else {
	            clientSupports = clientSupportRepository.findByStatus(statusDropdown);
	        }
	        model.addAttribute("selected", statusDropdown);
	    } else {
	        // No filters applied, return all records
	        clientSupports = clientSupportRepository.findByActiveStatusTrue();
	    }
	    model.addAttribute("clientSupports", clientSupports);
	    return "clientsupportlist";
	}*/
	
	@GetMapping("/viewclientsupport")
	public String filtersWithList(
	    @RequestParam(value = "supportReqyestType", required = false) String supportRequesttype,
	    @RequestParam(value = "statusDropdown", required = false) String statusDropdown,
	    @RequestParam(value = "searchTerm", required = false) String searchTerm,
	    Model model,
	    Principal principal // Add Principal parameter to retrieve logged-in user information
	) {
	    String username = principal.getName(); // Retrieve the currently logged-in user's email

	    // Retrieve the user's store code based on their email (assuming you have a method to do this)
	    String userStoreCode = userService.getUserStoreCodeByUserName(username); // Replace userService with your actual service

	    List<ClientSupport> clientSupports;

	    if (searchTerm != null && !searchTerm.isEmpty()) {
	        // Filter by search term across multiple fields
	        clientSupports = clientSupportRepository.findBySearchTermAndStoreCode(searchTerm, userStoreCode);
	        model.addAttribute("searchTerm", searchTerm);
	    } else if (supportRequesttype != null && !supportRequesttype.isEmpty()) {
	        // Filter by support request type and store code
	        if ("All".equalsIgnoreCase(supportRequesttype)) {
	            clientSupports = clientSupportRepository.findBySupportRequestTypeAndStoreCode(supportRequesttype, userStoreCode);
	        } else {
	            clientSupports = clientSupportRepository.findBySupportRequestTypeContainingAndStoreCode(supportRequesttype, userStoreCode);
	        }
	        model.addAttribute("selectedType", supportRequesttype);
	    } else if (statusDropdown != null && !statusDropdown.isEmpty()) {
	        // Filter by status dropdown and store code
	        if ("All".equalsIgnoreCase(statusDropdown)) {
	            clientSupports = clientSupportRepository.findByStatusAndStoreCode(statusDropdown, userStoreCode);
	        } else {
	            clientSupports = clientSupportRepository.findByStatusAndStoreCode(statusDropdown, userStoreCode);
	        }
	        model.addAttribute("selected", statusDropdown);
	    } else {
	        // No filters applied, return all records for the user's store code
	        clientSupports = clientSupportRepository.findByStoreCode(userStoreCode);
	    }
	    model.addAttribute("clientSupports", clientSupports);
	    return "clientsupportlist";
	}


	@GetMapping("/viewsupportclient/{id}")
	public String viewSupportdetails(@PathVariable Long id, Model model) {
		model.addAttribute("clientSupport", clientSupportService.getClientSupportById(id));
		return "clientsupportcapturecomentpop";
	}
	
	@PostMapping("/saveComment/{id}")
	public String saveComments(@PathVariable Long id,
	                           @ModelAttribute("adminComments") AdminComments adminComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddSupportAdmin entity
	    AddSupportAdmin existingAddSupportAdmin = addSupportAdminService.getAddSupportAdminById(id);
	    
	    ClientSupport existingClientSupport = clientSupportService.getClientSupportById(id);

	    // Create a new AdminComments
	    AdminComments comment = new AdminComments();
	    comment.setAddSupportAdmin(existingAddSupportAdmin);
	    comment.setClientSupport(existingClientSupport);
	    comment.setAdminComments(commentText);
	    comment.setAddedBy("Client");
	    comment.setRequestStatus(true); // Set "yes" if true, "no" otherwise
	    comment.setReason("Regular Comment");
	    comment.setDateAdded(new Date());
	    comment.setTimeAdded(new Date());

	    // Set the supportRequestId indirectly
	    comment.setSupportRequestId(existingClientSupport.getSupportRequestId());

	    // Save the comment
	    admincommentService.saveComment(comment);

	    // Redirect to the support view page
	    return "redirect:/viewsupport";
	}
	
	@GetMapping("/close/{id}")
	public String closed(@PathVariable Long id,Model model) {
		model.addAttribute("clientSupport", clientSupportService.getClientSupportById(id));
		return "closesupportrequestpop";
	}
	
	@PostMapping("/closesupportrequest/{id}")
	public String saveCloseComments(@PathVariable Long id,
	                           @ModelAttribute("adminComments") AdminComments adminComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddSupportAdmin entity
	    AddSupportAdmin existingAddSupportAdmin = addSupportAdminService.getAddSupportAdminById(id);
	    
	    ClientSupport existingClientSupport = clientSupportService.getClientSupportById(id);
	    
		existingClientSupport.setStatus("Closed");
		   clientSupportService.saveClientSupport(existingClientSupport);


	    // Create a new AdminComments
	    AdminComments comment = new AdminComments();
	    comment.setAddSupportAdmin(existingAddSupportAdmin);
	    comment.setClientSupport(existingClientSupport);
	    comment.setAddedBy("Client");
	    comment.setAdminComments(commentText);
	    comment.setRequestStatus(true);
	    comment.setReason("client opted to close the support request");
	    comment.setDateAdded(new Date());
	    comment.setTimeAdded(new Date());

	    // Set the supportRequestId indirectly
	    comment.setSupportRequestId(existingAddSupportAdmin.getSupportRequestId());
	    
	    // Save the comment
	    admincommentService.saveComment(comment);

	    // Handle requestStatus as needed

	    // Redirect to the support view page
	    return "redirect:/viewsupport";
	}

	private List<String> base64ImageList; // Declare it as a class field

    @GetMapping("/clientsupportDetails/{id}")
    public String viewSupportDetails(@PathVariable Long id, Model model) {
        // Your existing code to populate addSupportAdmin and supportFilesList
    	 ClientSupport clientSupport = clientSupportService.getClientSupportById(id);

 	    // Get the list of SupportFiles associated with the AddSupportAdmin
 	    List<SupportFiles> supportFilesList = clientSupport.getAdminSupportFiles();
 	    
 	   List<AdminComments> adminCommentsList = clientSupport.getComments().stream()
 		        .filter(AdminComments::getRequestStatus) // Filter by getRequestStatus = true
 		        .collect(Collectors.toList());


 	    // Add the AddSupportAdmin and SupportFiles list to the model
 	    model.addAttribute("ClientSupport", clientSupport);

        // Convert SupportFiles data to Base64 and add to the model
        base64ImageList = new ArrayList<>();
        for (SupportFiles supportFile : supportFilesList) {
            if (supportFile.getFileData() != null) {
                String base64Image = Base64.getEncoder().encodeToString(supportFile.getFileData());
                base64ImageList.add(base64Image);
            } else {
                base64ImageList.add(""); // or null, depending on your preference
            }
        }

        model.addAttribute("clientSupport", clientSupport);
        model.addAttribute("base64ImageList", base64ImageList);
        model.addAttribute("adminCommentsList", adminCommentsList);

        return "clientviewsupportdts";
    }

    @GetMapping("/downloads/{id}")
    public ResponseEntity<ByteArrayResource> downloadImage(@PathVariable Integer id) {
        // Assuming id is the index of the image in the list

        if (id >= 0 && id < base64ImageList.size()) {
            String base64Image = base64ImageList.get(id);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            ByteArrayResource resource = new ByteArrayResource(imageBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "image.png");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(imageBytes.length)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
