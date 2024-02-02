package com.project.Fabo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

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
import com.project.Fabo.entity.SupportFiles;
import com.project.Fabo.entity.Client;
import com.project.Fabo.entity.ClientSupport;
import com.project.Fabo.repository.ClientRepository;
import com.project.Fabo.service.AddSupportAdminService;
import com.project.Fabo.service.ClientService;
import com.project.Fabo.service.ClientSupportService;
import com.project.Fabo.service.AdminCommentService;
import com.project.Fabo.service.SupportMirrorService;

import com.project.Fabo.repository.AddSupportAdminRepository;


@Controller
public class AdminSupportController {
	
	private AddSupportAdminService addSupportAdminService;

	private AddSupportAdminRepository addSupportAdminRepository;
	
	private ClientRepository clientRepository;
	
	private SupportMirrorService supportMirrorService;
	
	private ClientService clientService;
	
	private ClientSupportService clientSupportService;
	
	private AdminCommentService admincommentService;


	public AdminSupportController(AddSupportAdminService addSupportAdminService,
			AddSupportAdminRepository addSupportAdminRepository, ClientRepository clientRepository,
			SupportMirrorService supportMirrorService, ClientService clientService,
			ClientSupportService clientSupportService, AdminCommentService admincommentService,
			List<String> base64ImageList) {
		super();
		this.addSupportAdminService = addSupportAdminService;
		this.addSupportAdminRepository = addSupportAdminRepository;
		this.clientRepository = clientRepository;
		this.supportMirrorService = supportMirrorService;
		this.clientService = clientService;
		this.clientSupportService = clientSupportService;
		this.admincommentService = admincommentService;
		this.base64ImageList = base64ImageList;
	}


	@GetMapping("/addsupport")
	public String addSupportAdmin(Model model) {
		List<Client> clients;
		clients = clientRepository.findAll();
		AddSupportAdmin addSupportAdmin = new AddSupportAdmin();
		model.addAttribute("addSupportAdmin",addSupportAdmin);
		 model.addAttribute("clients", clients);
		return "addsupportadmin";
	}

	
	@PostMapping("/createSupportAdmin")
	public String saveSupportAdmin(@ModelAttribute("addSupportAdmin") AddSupportAdmin addSupportAdmin,
	        @RequestParam("storeName") String storeName,
	        @RequestParam(value = "files", required = false) List<MultipartFile> files) {
	    // Set default values for internalComments, externalComments, and status
	    addSupportAdmin.setInternalComments("No comments");
	    addSupportAdmin.setCommentsToClient("No comments");
	    addSupportAdmin.setCommentsFromClient("No comments");
	    addSupportAdmin.setStatus("New");
	    // Handle file uploads
	    addSupportAdminService.saveAddSupportAdmin(addSupportAdmin);
	    
	    supportMirrorService.addSupportRecordToBothSides(addSupportAdmin);

	    ClientSupport clientSupport = clientSupportService.getClientSupportById(addSupportAdmin.getId());


	    
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
        addSupportAdmin.setAdminSupportFiles(adminSupportFiles);
        
        addSupportAdminService.saveAddSupportAdmin(addSupportAdmin);

	    return "redirect:/viewsupport";
	}
	
	 @GetMapping("/clientstoreinfo/{id}")
	    public String viewClientInfo(@PathVariable Long id, Model model) {
	        AddSupportAdmin addSupportAdmin = addSupportAdminService.getAddSupportAdminById(id);

	        if (addSupportAdmin != null) {
	            // Fetch associated Client information based on the storeCode in the Invoice
	            Client client = clientService.getClientByStoreCode(addSupportAdmin.getStoreCode());

	            // Add the Invoice and Client information to the model
	            model.addAttribute("addSupportAdmin", addSupportAdmin);
	            model.addAttribute("client", client);
	        }

	        return "supportadminviewpop";
	    }

	/*
	@GetMapping("/viewsupport")
	public String viewSupportlist(
	    @RequestParam(value = "search", required = false) String search,
	    @RequestParam(value = "invoicedropdown", required = false) String invoicedropdown,
	    @RequestParam(value = "statusDropdown", required = false) String statusDropdown,
	    Model model
	) {
	    List<AddSupportAdmin> addSupportAdmins;

	    if ("All".equalsIgnoreCase(invoicedropdown) && "All".equalsIgnoreCase(statusDropdown)) {
	        addSupportAdmins = addSupportAdminRepository.findByActiveStatusTrue();
	    } else if (!"All".equalsIgnoreCase(invoicedropdown) && !"All".equalsIgnoreCase(statusDropdown)) {
	        addSupportAdmins = addSupportAdminRepository.findBySupportRequestTypeContainingAndStatus(invoicedropdown, statusDropdown);
	    } else if (!"All".equalsIgnoreCase(invoicedropdown)) {
	        addSupportAdmins = addSupportAdminRepository.findBySupportRequestTypeContaining(invoicedropdown);
	    } else {
	        addSupportAdmins = addSupportAdminRepository.findByStatus(statusDropdown);
	    }

	    model.addAttribute("addSupportAdmins", addSupportAdmins);
	    model.addAttribute("selectedType", invoicedropdown);
	    model.addAttribute("selected", statusDropdown);

	    return "supportlistadmin";
	}
	*/
		/*@GetMapping("/viewsupport")
		public String filterSupportList(
		        @RequestParam(value = "invoicedropdown", required = false) String invoicedropdown,
		        @RequestParam(value = "statusDropdown", required = false) String statusDropdown,
		        @RequestParam(value = "searchTerm", required = false) String searchTerm,
		        Model model
		) {
		    List<AddSupportAdmin> addSupportAdmins;

		    if (searchTerm != null && !searchTerm.isEmpty()) {
		        // Filter by search term across multiple fields
		        addSupportAdmins = addSupportAdminRepository.findBySearchTerm(searchTerm);
		        model.addAttribute("searchTerm", searchTerm);
		    } else if (invoicedropdown != null && !invoicedropdown.isEmpty()) {
		        // Filter by invoice dropdown
		        if ("All".equalsIgnoreCase(invoicedropdown)) {
		            addSupportAdmins = addSupportAdminRepository.findByActiveStatusTrue();
		        } else {
		            addSupportAdmins = addSupportAdminRepository.findBySupportRequestTypeContaining(invoicedropdown);
		        }
		        model.addAttribute("selectedType", invoicedropdown);
		    } else if (statusDropdown != null && !statusDropdown.isEmpty()) {
		        // Filter by status dropdown
		        if ("All".equalsIgnoreCase(statusDropdown)) {
		            addSupportAdmins = addSupportAdminRepository.findByActiveStatusTrue();
		        } else {
		            addSupportAdmins = addSupportAdminRepository.findByStatus(statusDropdown);
		        }
		        model.addAttribute("selected", statusDropdown);
		    } else {
		        // No filters applied, return all records
		        addSupportAdmins = addSupportAdminRepository.findByActiveStatusTrue();
		    }  
		    
		    List<String> storeCodes = addSupportAdminRepository.findDistinctStoreCodes();
		    model.addAttribute("storeCodes", storeCodes);
		    model.addAttribute("addSupportAdmins", addSupportAdmins);

		    return "supportlistadmin";
		}*/
	 @GetMapping("/viewsupport")
	 public String filterSupportList(
	         @RequestParam(value = "invoicedropdown", required = false) String invoicedropdown,
	         @RequestParam(value = "statusDropdown", required = false) String statusDropdown,
	         @RequestParam(value = "searchTerm", required = false) String searchTerm,
	         Model model
	 ) {
	     List<AddSupportAdmin> addSupportAdmins;

	     // Log store code retrieval
	     List<String> storeCodes = addSupportAdminRepository.findDistinctStoreCodes();


	     if (searchTerm != null && !searchTerm.isEmpty()) {
	         // Filter by search term across multiple fields
	         addSupportAdmins = addSupportAdminRepository.findBySearchTerm(searchTerm);
	         model.addAttribute("searchTerm", searchTerm);
	     } else if (invoicedropdown != null && !invoicedropdown.isEmpty()) {
	         // Filter by invoice dropdown
	         if ("All".equalsIgnoreCase(invoicedropdown)) {
	             addSupportAdmins = addSupportAdminRepository.findByActiveStatusTrue();
	         } else {
	             addSupportAdmins = addSupportAdminRepository.findBySupportRequestTypeContaining(invoicedropdown);
	         }
	         model.addAttribute("selectedType", invoicedropdown);
	     } else if (statusDropdown != null && !statusDropdown.isEmpty()) {
	         // Filter by status dropdown
	         if ("All".equalsIgnoreCase(statusDropdown)) {
	             addSupportAdmins = addSupportAdminRepository.findByActiveStatusTrue();
	         } else {
	             addSupportAdmins = addSupportAdminRepository.findByStatus(statusDropdown);
	         }
	         model.addAttribute("selected", statusDropdown);
	     } else {
	         // No filters applied, return all records
	         addSupportAdmins = addSupportAdminRepository.findByActiveStatusTrue();
	     }  

	     model.addAttribute("storeCodes", storeCodes);
	     model.addAttribute("addSupportAdmins", addSupportAdmins);

	     return "supportlistadmin";
	 }

	@GetMapping("/inprogress/{id}")
	public String inProgress(@PathVariable Long id,Model model) {
		model.addAttribute("addSupportAdmin", addSupportAdminService.getAddSupportAdminById(id));
		return "supportlistaddmininprogresspop";
	}
	
	@PostMapping("/inProgress/{id}")
	public String saveInprogressComments(@PathVariable Long id,
	                           @ModelAttribute("adminComments") AdminComments adminComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddSupportAdmin entity
	    AddSupportAdmin existingAddSupportAdmin = addSupportAdminService.getAddSupportAdminById(id);
	    
	    ClientSupport existingClientSupport = clientSupportService.getClientSupportById(id);
	    
		   existingAddSupportAdmin.setStatus("In-Progress");
		   addSupportAdminService.saveAddSupportAdmin(existingAddSupportAdmin);

	    // Create a new AdminComments
	    AdminComments comment = new AdminComments();
	    comment.setAddSupportAdmin(existingAddSupportAdmin);
	    comment.setClientSupport(existingClientSupport);
	    comment.setAddedBy("Admin");
	    comment.setAdminComments(commentText);
	    comment.setRequestStatus(true);
	    comment.setReason("Opted to change status to In-Progress");
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
	
	@GetMapping("/closed/{id}")
	public String closed(@PathVariable Long id,Model model) {
		model.addAttribute("addSupportAdmin", addSupportAdminService.getAddSupportAdminById(id));
		return "closesupportlistpop";
	}
	
	@PostMapping("/closepop/{id}")
	public String saveCloseComments(@PathVariable Long id,
	                           @ModelAttribute("adminComments") AdminComments adminComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddSupportAdmin entity
	    AddSupportAdmin existingAddSupportAdmin = addSupportAdminService.getAddSupportAdminById(id);
	    
	    ClientSupport existingClientSupport = clientSupportService.getClientSupportById(id);
	    
		  
		   existingAddSupportAdmin.setStatus("Closed");
		   addSupportAdminService.saveAddSupportAdmin(existingAddSupportAdmin);


	    // Create a new AdminComments
	    AdminComments comment = new AdminComments();
	    comment.setAddSupportAdmin(existingAddSupportAdmin);
	    comment.setClientSupport(existingClientSupport);
	    comment.setAddedBy("Admin");
	    comment.setAdminComments(commentText);
	    comment.setRequestStatus(true);
	    comment.setReason("opted to close the support request");
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

	
	@GetMapping("/reopen/{id}")
	public String reopen(@PathVariable Long id,Model model) {
		model.addAttribute("addSupportAdmin", addSupportAdminService.getAddSupportAdminById(id));
		return "reopensupportpop";
	}
	
	@PostMapping("/reOpen/{id}")
	public String saveReopenComments(@PathVariable Long id,
	                           @ModelAttribute("adminComments") AdminComments adminComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddSupportAdmin entity
	    AddSupportAdmin existingAddSupportAdmin = addSupportAdminService.getAddSupportAdminById(id);
	    
	    ClientSupport existingClientSupport = clientSupportService.getClientSupportById(id);
	    
		   existingAddSupportAdmin.setStatus("Re-Open");
		   addSupportAdminService.saveAddSupportAdmin(existingAddSupportAdmin);

	    // Create a new AdminComments
	    AdminComments comment = new AdminComments();
	    comment.setAddSupportAdmin(existingAddSupportAdmin);
	    comment.setClientSupport(existingClientSupport);
	    comment.setAddedBy("Admin");
	    comment.setAdminComments(commentText);
	    comment.setRequestStatus(true);
	    comment.setReason("Opted to Re-open the support request");
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
	
	
	@GetMapping("/supportlistaddmincapurepop/{id}")
	public String viewSupportCommentsPop(@PathVariable Long id,Model model) {
		model.addAttribute("addSupportAdmin", addSupportAdminService.getAddSupportAdminById(id));
		return "supportlistaddmincapurepop";
	}
	
	@PostMapping("/saveComments/{id}")
	public String saveComments(@PathVariable Long id,
	                           @ModelAttribute("adminComments") AdminComments adminComments,
	                           @RequestParam("commentText") String commentText,
	                           @RequestParam(value = "requestStatus", defaultValue = "false") boolean requestStatus,
	                           Model model) {

	    // Retrieve the AddSupportAdmin entity
	    AddSupportAdmin existingAddSupportAdmin = addSupportAdminService.getAddSupportAdminById(id);
	    
	    ClientSupport existingClientSupport = clientSupportService.getClientSupportById(id);

	    // Create a new AdminComments
	    AdminComments comment = new AdminComments();
	    comment.setAddSupportAdmin(existingAddSupportAdmin);
	    comment.setClientSupport(existingClientSupport);
	    comment.setAddedBy("Admin");
	    comment.setAdminComments(commentText);
	    comment.setRequestStatus(requestStatus); // Set "yes" if true, "no" otherwise
	    comment.setReason("Regular Comment");
	    comment.setDateAdded(new Date());
	    comment.setTimeAdded(new Date());

	    // Set the supportRequestId indirectly
	    comment.setSupportRequestId(existingAddSupportAdmin.getSupportRequestId());

	    // Save the comment
	    admincommentService.saveComment(comment);

	    // Redirect to the support view page
	    return "redirect:/viewsupport";
	}


	
	@GetMapping("/viewsupportadmindts/comment/{id}")
	public String viewSupportComment(@PathVariable Long id, Model model) {
		model.addAttribute("addSupportAdmin", addSupportAdminService.getAddSupportAdminById(id));
		return "supportlistpop";
	}
	
	/*@GetMapping("/supportDetails/{id}")
	public String viewSupportDetails(@PathVariable Long id, Model model) {
	    // Get AddSupportAdmin entity by ID
	    AddSupportAdmin addSupportAdmin = addSupportAdminService.getAddSupportAdminById(id);

	    // Get the list of SupportFiles associated with the AddSupportAdmin
	    List<SupportFiles> supportFilesList = addSupportAdmin.getAdminSupportFiles();

	    // Add the AddSupportAdmin and SupportFiles list to the model
	    model.addAttribute("addSupportAdmin", addSupportAdmin);
	    
	    // Convert SupportFiles data to Base64 and add to the model
	    List<String> base64ImageList = new ArrayList<>();
	    for (SupportFiles supportFile : supportFilesList) {
	        if (supportFile.getFileData() != null) {
	            String base64Image = Base64.getEncoder().encodeToString(supportFile.getFileData());
	            base64ImageList.add(base64Image);
	        } else {
	            base64ImageList.add(""); // or null, depending on your preference
	        }
	    }
	    model.addAttribute("base64ImageList", base64ImageList);

	    return "viewsupportadmindts";
	}*/
	
	 private List<String> base64ImageList; // Declare it as a class field

	    @GetMapping("/supportDetails/{id}")
	    public String viewSupportDetails(@PathVariable Long id, Model model) {
	        // Your existing code to populate addSupportAdmin and supportFilesList
	    	 AddSupportAdmin addSupportAdmin = addSupportAdminService.getAddSupportAdminById(id);

	 	    // Get the list of SupportFiles associated with the AddSupportAdmin
	 	    List<SupportFiles> supportFilesList = addSupportAdmin.getAdminSupportFiles();
	 	    
	 	   List<AdminComments> adminCommentsList = addSupportAdmin.getComments();

	 	    // Add the AddSupportAdmin and SupportFiles list to the model
	 	    model.addAttribute("addSupportAdmin", addSupportAdmin);

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

	        model.addAttribute("addSupportAdmin", addSupportAdmin);
	        model.addAttribute("base64ImageList", base64ImageList);
	        model.addAttribute("adminCommentsList", adminCommentsList);

	        return "viewsupportadmindts";
	    }

	    @GetMapping("/download/{id}")
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

	
	 @GetMapping("/deleterequest/{id}")
	    public String deleteSupportAdmin(@PathVariable Long id) {
	        addSupportAdminService.deleteAddSupportAdminById(id);
	        clientSupportService.deleteClientSuppportById(id);
	        return "redirect:/viewsupport";
	    }

}
