package com.project.Fabo.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.Fabo.entity.Client;
import com.project.Fabo.entity.Invoice;
import com.project.Fabo.entity.InvoiceComments;
import com.project.Fabo.entity.InvoiceFile;
import com.project.Fabo.repository.ClientRepository;
import com.project.Fabo.repository.InvoiceRepository;
import com.project.Fabo.service.ClientService;
import com.project.Fabo.service.InvoiceCommentsService;
import com.project.Fabo.service.InvoiceService;
import com.project.Fabo.service.UserService;

@Controller
public class InvoiceController {
	
	 private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

	private InvoiceService invoiceService;
	private InvoiceRepository invoiceRepository;
	private ClientRepository clientRepository;
	private ClientService clientService;
	private InvoiceCommentsService invoiceCommentsService;
	private UserService userService;

		public InvoiceController(InvoiceService invoiceService, InvoiceRepository invoiceRepository,
			ClientRepository clientRepository, ClientService clientService,
			InvoiceCommentsService invoiceCommentsService, UserService userService, List<String> base64ImageList) {
		super();
		this.invoiceService = invoiceService;
		this.invoiceRepository = invoiceRepository;
		this.clientRepository = clientRepository;
		this.clientService = clientService;
		this.invoiceCommentsService = invoiceCommentsService;
		this.userService = userService;
		this.base64ImageList = base64ImageList;
	}

		//calling addinvoice form
	    @GetMapping("/addinvoice")
	    public String showaddInvoiceForm(Model model) {
	    	List<Client> clients;
			clients = clientRepository.findAll();
	        model.addAttribute("invoice", new Invoice());
	        model.addAttribute("clients", clients);
	        return "addinvoiceadmin"; 
	    }
	   
	    @PostMapping("/createInvoice")
	    public String saveInvoice(@ModelAttribute("invoice") Invoice invoice,
	                             @RequestParam("storeCode") String storeCode,
	                             @RequestParam("invoiceComments") String invoiceComments,
	                             @RequestParam("invoiceType") String invoiceType,
	                             @RequestParam(value = "files", required = false) List<MultipartFile> files) {
	    	 log.info("Number of files received: " + (files != null ? files.size() : 0));
	        invoice.setInvoiceStatus("Pending");

	        // Handle file uploads
	        List<InvoiceFile> invoiceFiles = new ArrayList<>();

	        if (files != null && !files.isEmpty()) {
	            for (MultipartFile file : files) {
	                try {
	                    InvoiceFile invoiceFile = new InvoiceFile();
	                    invoiceFile.setFileData(file.getBytes());
	                    invoiceFile.setInvoice(invoice);
	                    invoiceFiles.add(invoiceFile);
	                } catch (IOException e) {
	                    // Handle the IOException as needed
	                    e.printStackTrace(); // Log the exception or throw a custom exception
	                }
	            }
	        }

	        // Associate files with the invoice
	        invoice.setInvoiceFiles(invoiceFiles);

	        // Save the invoice with associated files
	        invoiceService.saveInvoice(invoice);
	        
	        
	        InvoiceComments comment = new InvoiceComments();
		    comment.setInvoice(invoice);
		    comment.setAddedBy("Admin");
		    comment.setAdminComments(invoiceComments);
		    comment.setRequestStatus(true); // Set "yes" if true, "no" otherwise
		    comment.setReason("Regular comments By Admin");
		    comment.setDateAdded(new Date());
		    comment.setTimeAdded(new Date());

		    invoiceCommentsService.saveComment(comment);
	        
	        return "redirect:/viewinvoice";
	    }


	    @RequestMapping(value = {"/viewinvoice", "/viewinvoice"}, method = {RequestMethod.GET, RequestMethod.POST})
	    public String listAndSearchInvoices(
	            @RequestParam(value = "search", required = false) String search,
	            @RequestParam(value = "invoiceType", required = false) String invoiceType,
	            Model model) {

	        // Retrieve all distinct invoice types for the filter dropdown
	        List<String> distinctInvoiceTypes = invoiceRepository.findDistinctInvoiceType();
	        model.addAttribute("distinctInvoiceTypes", distinctInvoiceTypes);

	        // Retrieve all distinct store codes for pre-populating the dropdown
	        List<String> storeCodes = invoiceRepository.findDistinctStoreCodes();
	        model.addAttribute("storeCodes", storeCodes);
 
	        // Retrieve all invoices from the service as a List
	        List<Invoice> invoices;

	        if (search != null && !search.isEmpty()) {
	            // If search parameter is provided, filter by search term
	            invoices = invoiceRepository.findBySearchTerm(search);
	        } else if (invoiceType != null && !invoiceType.isEmpty()) {
	            // If invoiceType parameter is provided, filter by invoice type
	            invoices = invoiceRepository.findByInvoiceType(invoiceType);
	        } else {
	            // Otherwise, retrieve all invoices
	            invoices = invoiceRepository.findByActiveStatusTrue();
	        }

	        // Add the list of invoices, search parameter, selected invoiceType, and store codes to the model
	        model.addAttribute("invoices", invoices);
	        model.addAttribute("search", search);
	        model.addAttribute("selectedInvoiceType", invoiceType);

	        return "invoiceslistadmin";
	    }
	    
	  
	    //View
	    @GetMapping("/viewinvoicedetails/{id}")
	    public String viewInvoiceDetails(@PathVariable Long id, Model model) {
	    	model.addAttribute("invoice", invoiceService.getInvoiceById(id));
	    	return "viewadmininvoicedts";
	    }
	    
	    @GetMapping("/clientstoreview/{id}")
	    public String viewClientInfo(@PathVariable Long id, Model model) {
	        Invoice invoice = invoiceService.getInvoiceById(id);

	        if (invoice != null) {
	            // Fetch associated Client information based on the storeCode in the Invoice
	            Client client = clientService.getClientByStoreCode(invoice.getStoreCode());

	            // Add the Invoice and Client information to the model
	            model.addAttribute("invoice", invoice);
	            model.addAttribute("client", client);
	        }

	        return "accountstrorecontactinfoadmin";
	    }
	    
	    
	    @GetMapping("/capture/{id}")
	    public String capturepopup(@PathVariable Long id,Model model) {
	    	model.addAttribute("invoice", invoiceService.getInvoiceById(id));
	    	return "invoicecapturepaypop";
	    }
	    
	    @PostMapping("/captureComment/{id}")
		public String saveComments(@PathVariable Long id,
		                           @ModelAttribute("invoiceComments") InvoiceComments invoiceComments,
		                           @RequestParam("commentText") String commentText,
		                           @RequestParam(value = "requestStatus", defaultValue = "false") boolean requestStatus,
		                           Model model) {

		    // Retrieve the AddSupportAdmin entity
		    Invoice existingInvoice = invoiceService.getInvoiceById(id);

	    	existingInvoice.setInvoiceStatus("Paid");
	    	invoiceService.saveInvoice(existingInvoice);
		    
		  

		    // Create a new AdminComments
		    InvoiceComments comment = new InvoiceComments();
		    comment.setInvoice(existingInvoice);
		    comment.setAddedBy("Admin");
		    comment.setAdminComments(commentText);
		    comment.setRequestStatus(requestStatus); // Set "yes" if true, "no" otherwise
		    comment.setReason("Admin marked as Paid");
		    comment.setDateAdded(new Date());
		    comment.setTimeAdded(new Date());

		    invoiceCommentsService.saveComment(comment);

		    // Redirect to the support view page
		    return "redirect:/viewinvoice";
		}


	    
	    @GetMapping("/uncapture/{id}")
	    public String uncapturepopup(@PathVariable Long id,Model model) {
	    	model.addAttribute("invoice", invoiceService.getInvoiceById(id));
	    	return "invoiceuncapturepaypop";
	    }
	    
	    @PostMapping("/uncaptureComment/{id}")
		public String uncaptureComments(@PathVariable Long id,
		                           @ModelAttribute("invoiceComments") InvoiceComments invoiceComments,
		                           @RequestParam("commentText") String commentText,
		                           @RequestParam(value = "requestStatus", defaultValue = "false") boolean requestStatus,
		                           Model model) {

		    // Retrieve the AddSupportAdmin entity
		    Invoice existingInvoice = invoiceService.getInvoiceById(id);

	    	existingInvoice.setInvoiceStatus("Pending");
	    	invoiceService.saveInvoice(existingInvoice);
		    
		  

		    // Create a new AdminComments
		    InvoiceComments comment = new InvoiceComments();
		    comment.setInvoice(existingInvoice);
		    comment.setAddedBy("Admin");
		    comment.setAdminComments(commentText);
		    comment.setRequestStatus(requestStatus); // Set "yes" if true, "no" otherwise
		    comment.setReason("Admin marked as Pending");
		    comment.setDateAdded(new Date());
		    comment.setTimeAdded(new Date());

		    invoiceCommentsService.saveComment(comment);

		    // Redirect to the support view page
		    return "redirect:/viewinvoice";
		}
	    
	   /* @PostMapping("/saveCommentToClient")
	    public String saveComments(
	            @RequestParam("id") Long id,
	            @RequestParam("commentText") String commentText,
	            @RequestParam("clientVisible") String clientVisible,
	            @RequestParam("requestStatus") String requestStatus
	    ) {
	        invoiceService.saveCommentAndStatusById(id, commentText, clientVisible, requestStatus);

	        return "redirect:/viewinvoice";
	    }*/
	    
	    /*@RequestMapping(value = {"/viewinvoices", "/viewinvoices"}, method = {RequestMethod.GET, RequestMethod.POST})
	    public String listAndSearchInvoice(
	            @RequestParam(value = "search", required = false) String search,
	            @RequestParam(value = "invoiceType", required = false) String invoiceType,
	            Model model) {

	        // Retrieve all distinct invoice types for the filter dropdown
	        List<String> distinctInvoiceTypes = invoiceRepository.findDistinctInvoiceType();
	        model.addAttribute("distinctInvoiceTypes", distinctInvoiceTypes);

	        // Retrieve all distinct invoice numbers for pre-populating the dropdown
	        List<String> invoiceNumbers = invoiceRepository.findDistinctInvoiceNumbers();
	        model.addAttribute("invoiceNumbers", invoiceNumbers);

	        // Retrieve all invoices from the service as a List
	        List<Invoice> invoices;

	        if (search != null && !search.isEmpty()) {
	            // If search parameter is provided, filter by search term
	            invoices = invoiceRepository.findBySearchTerm(search);
	        } else if (invoiceType != null && !invoiceType.isEmpty()) {
	            // If invoiceType parameter is provided, filter by invoice type
	            invoices = invoiceRepository.findByInvoiceType(invoiceType);
	        } else {
	            // Otherwise, retrieve all invoices
	            invoices = invoiceRepository.findByActiveStatusTrue();
	        }

	        // Add the list of invoices, search parameter, and selected invoiceType to the model
	        model.addAttribute("invoices", invoices);
	        model.addAttribute("search", search);
	        model.addAttribute("selectedInvoiceType", invoiceType);

	        return "clientinvoiceslist"; 
	    }*/

	    @RequestMapping(value = {"/viewinvoices", "/viewinvoices"}, method = {RequestMethod.GET, RequestMethod.POST})
	    public String listAndSearchInvoice(
	            @RequestParam(value = "search", required = false) String search,
	            @RequestParam(value = "invoiceType", required = false) String invoiceType,
	            Model model,
	            Principal principal // Add Principal parameter to retrieve logged-in user information
	    ) {
	        // Retrieve the currently logged-in user's username
	        String username = principal.getName();

	        // Retrieve the user's store code based on their username
	        String userStoreCode = userService.getUserStoreCodeByUserName(username); // Replace userService with your actual service

	        // Retrieve all distinct invoice types for the filter dropdown
	        List<String> distinctInvoiceTypes = invoiceRepository.findDistinctInvoiceType();
	        model.addAttribute("distinctInvoiceTypes", distinctInvoiceTypes);

	        // Retrieve all distinct invoice numbers for pre-populating the dropdown
	        List<String> invoiceNumbers = invoiceRepository.findDistinctInvoiceNumbers();
	        model.addAttribute("invoiceNumbers", invoiceNumbers);

	        // Retrieve all invoices from the service as a List based on user's store code and active status true
	        List<Invoice> invoices;

	        if (search != null && !search.isEmpty()) {
	            // If search parameter is provided, filter by search term
	            invoices = invoiceRepository.findBySearchTermAndStoreCodeAndActiveStatusTrue(search, userStoreCode);
	        } else if (invoiceType != null && !invoiceType.isEmpty()) {
	            // If invoiceType parameter is provided, filter by invoice type
	            invoices = invoiceRepository.findByInvoiceTypeAndStoreCodeAndActiveStatusTrue(invoiceType, userStoreCode);
	        } else {
	            // Otherwise, retrieve all invoices for the user's store code with active status true
	            invoices = invoiceRepository.findByStoreCodeAndActiveStatusTrue(userStoreCode);
	        }

	        // Add the list of invoices, search parameter, and selected invoiceType to the model
	        model.addAttribute("invoices", invoices);
	        model.addAttribute("search", search);
	        model.addAttribute("selectedInvoiceType", invoiceType);

	        return "clientinvoiceslist"; 
	    }

	    
		 private List<String> base64ImageList; // Declare it as a class field

		    @GetMapping("/invoiceDetails/{id}")
		    public String viewInvoicesDetails(@PathVariable Long id, Model model) {
		        // Your existing code to populate addSupportAdmin and supportFilesList
		    	 Invoice invoice = invoiceService.getInvoiceById(id);

		 	    // Get the list of SupportFiles associated with the AddSupportAdmin
		 	    List<InvoiceFile> invoiceFilesList = invoice.getInvoiceFiles();
		 	    
		 	   List<InvoiceComments> invoiceCommentsList = invoice.getComments();

		 	    // Add the AddSupportAdmin and SupportFiles list to the model
		 	    model.addAttribute("invoice", invoice);

		        // Convert SupportFiles data to Base64 and add to the model
		        base64ImageList = new ArrayList<>();
		        for (InvoiceFile invoiceFile : invoiceFilesList) {
		            if (invoiceFile.getFileData() != null) {
		                String base64Image = Base64.getEncoder().encodeToString(invoiceFile.getFileData());
		                base64ImageList.add(base64Image);
		            } else {
		                base64ImageList.add(""); // or null, depending on your preference
		            }
		        }

		        model.addAttribute("invoice", invoice);
		        model.addAttribute("base64ImageList", base64ImageList);
		        model.addAttribute("invoiceCommentsList", invoiceCommentsList);

		        return "viewinvoicedetails";
		    }

		    @GetMapping("/downloaded/{id}")
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
		    
		    @GetMapping("/clientinvoiceDetails/{id}")
		    public String clientviewInvoicesDetails(@PathVariable Long id, Model model) {
		        // Your existing code to populate addSupportAdmin and supportFilesList
		    	 Invoice invoice = invoiceService.getInvoiceById(id);

		 	    // Get the list of SupportFiles associated with the AddSupportAdmin
		 	    List<InvoiceFile> invoiceFilesList = invoice.getInvoiceFiles();
		 	    
		 	   List<InvoiceComments> invoiceCommentsList = invoice.getComments().stream()
		 		        .filter(InvoiceComments::getRequestStatus) // Filter by getRequestStatus = true
		 		        .collect(Collectors.toList());

		 	    // Add the AddSupportAdmin and SupportFiles list to the model
		 	    model.addAttribute("invoice", invoice);

		        // Convert SupportFiles data to Base64 and add to the model
		        base64ImageList = new ArrayList<>();
		        for (InvoiceFile invoiceFile : invoiceFilesList) {
		            if (invoiceFile.getFileData() != null) {
		                String base64Image = Base64.getEncoder().encodeToString(invoiceFile.getFileData());
		                base64ImageList.add(base64Image);
		            } else {
		                base64ImageList.add(""); // or null, depending on your preference
		            }
		        }

		        model.addAttribute("invoice", invoice);
		        model.addAttribute("base64ImageList", base64ImageList);
		        model.addAttribute("invoiceCommentsList", invoiceCommentsList);

		        return "viewclientinvoicedts";
		    }

		    @GetMapping("/invoicedownload/{id}")
		    public ResponseEntity<ByteArrayResource> clientdownloadImage(@PathVariable Integer id) {
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
		    
		    @GetMapping("/invoice/delete/{id}")
		    public String deleteInvoice(@PathVariable Long id) {
		        invoiceService.deleteInvoiceById(id);
		        return "redirect:/viewinvoice";
		    }
    
}
