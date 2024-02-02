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

import com.project.Fabo.entity.AddProductAdmin;
import com.project.Fabo.entity.AdminProductComments;
import com.project.Fabo.entity.Client;
import com.project.Fabo.entity.ClientProduct;
import com.project.Fabo.entity.ProductFiles;
import com.project.Fabo.repository.AddProductAdminRepository;
import com.project.Fabo.repository.ClientRepository;
import com.project.Fabo.service.AddProductAdminService;
import com.project.Fabo.service.AdminProductCommentService;
import com.project.Fabo.service.ClientProductService;
import com.project.Fabo.service.ClientService;
import com.project.Fabo.service.ProductMirrorService;



@Controller
public class AdminProductController {
	
	private AddProductAdminService addProductAdminService;

	private AddProductAdminRepository addProductAdminRepository;
	
	private ClientRepository clientRepository;
	
	private ProductMirrorService productMirrorService;
	
	private ClientService clientService;
	
	private ClientProductService clientProductService;
	
	private AdminProductCommentService adminProductCommentService;

	public AdminProductController(AddProductAdminService addProductAdminService,
			AddProductAdminRepository addProductAdminRepository, ClientRepository clientRepository,
			ProductMirrorService productMirrorService, ClientService clientService,
			ClientProductService clientProductService, AdminProductCommentService adminProductCommentService) {
		super();
		this.addProductAdminService = addProductAdminService;
		this.addProductAdminRepository = addProductAdminRepository;
		this.clientRepository = clientRepository;
		this.productMirrorService = productMirrorService;
		this.clientService = clientService;
		this.clientProductService = clientProductService;
		this.adminProductCommentService = adminProductCommentService;
	}


	@GetMapping("/addproduct")
	public String addProductAdmin(Model model) {
		List<Client> clients;
		clients = clientRepository.findAll();
		AddProductAdmin addProductAdmin = new AddProductAdmin();
		model.addAttribute("addProductAdmin",addProductAdmin);
		 model.addAttribute("clients", clients);
		return "addproductadmin";
	} 

	
	@PostMapping("/createProductAdmin")
	public String saveProductAdmin(@ModelAttribute("addProductAdmin") AddProductAdmin addProductAdmin,
	        @RequestParam("storeName") String storeName,
	        @RequestParam(value = "files", required = false) List<MultipartFile> files) {
	    // Set default values for internalComments, externalComments, and status
	    addProductAdmin.setInternalComments("No comments");
	    addProductAdmin.setCommentsToClient("No comments");
	    addProductAdmin.setCommentsFromClient("No comments");
	    addProductAdmin.setStatus("New");
	    
	    System.out.println("Number of files received: " + (files != null ? files.size() : 0));
	    
	    // Handle file uploads
	    addProductAdminService.saveAddProductAdmin(addProductAdmin);
	    
	    productMirrorService.addProductRecordToBothSides(addProductAdmin);

	    ClientProduct clientProduct = clientProductService.getClientProductById(addProductAdmin.getId());


	    
        List<ProductFiles> adminProductFiles = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                	ProductFiles adminProductFile = new ProductFiles();
                	adminProductFile.setFileData(file.getBytes());
                	adminProductFile.setAddProductAdmin(addProductAdmin);
                	adminProductFile.setClientProduct(clientProduct);
                	adminProductFiles.add(adminProductFile);
                } catch (IOException e) {
                    // Handle the IOException as needed
                    e.printStackTrace(); // Log the exception or throw a custom exception
                }
            }
        }

        // Associate files with the invoice
        addProductAdmin.setAdminProductFiles(adminProductFiles);
        
        addProductAdminService.saveAddProductAdmin(addProductAdmin);

	    return "redirect:/viewproduct";
	}
	
	 @GetMapping("/productviewpop/{id}")
	    public String viewClientInfo(@PathVariable Long id, Model model) {
	        AddProductAdmin addProductAdmin = addProductAdminService.getAddProductAdminById(id);
 
	        if (addProductAdmin != null) {
	            // Fetch associated Client information based on the storeCode in the Invoice
	            Client client = clientService.getClientByStoreCode(addProductAdmin.getStoreCode());

	            // Add the Invoice and Client information to the model
	            model.addAttribute("addProductAdmin", addProductAdmin);
	            model.addAttribute("client", client);
	        }

	        return "productviewpop";
	    }

	@GetMapping("/viewproduct")
	public String filterProductList(
	        @RequestParam(value = "invoicedropdown", required = false) String invoicedropdown,
	        @RequestParam(value = "statusDropdown", required = false) String statusDropdown,
	        @RequestParam(value = "searchTerm", required = false) String searchTerm,
	        Model model
	) {
	    List<AddProductAdmin> addProductAdmins;

	    if (searchTerm != null && !searchTerm.isEmpty()) {
	        // Filter by search term across multiple fields
	        addProductAdmins = addProductAdminRepository.findBySearchTerm(searchTerm);
	        model.addAttribute("searchTerm", searchTerm);
	    } else if (invoicedropdown != null && !invoicedropdown.isEmpty()) {
	        // Filter by invoice dropdown
	        if ("All".equalsIgnoreCase(invoicedropdown)) {
	            addProductAdmins = addProductAdminRepository.findByActiveStatusTrue();
	        } else {
	            addProductAdmins = addProductAdminRepository.findByProductRequestTypeContaining(invoicedropdown);
	        }
	        model.addAttribute("selectedType", invoicedropdown);
	    } else if (statusDropdown != null && !statusDropdown.isEmpty()) {
	        // Filter by status dropdown
	        if ("All".equalsIgnoreCase(statusDropdown)) {
	            addProductAdmins = addProductAdminRepository.findByActiveStatusTrue();
	        } else {
	            addProductAdmins = addProductAdminRepository.findByStatus(statusDropdown);
	        }
	        model.addAttribute("selected", statusDropdown);
	    } else {
	        // No filters applied, return all records
	        addProductAdmins = addProductAdminRepository.findByActiveStatusTrue();
	    }  
	    List<String> storeCodes = addProductAdminRepository.findDistinctStoreCodes();
	    model.addAttribute("storeCodes", storeCodes);

	    model.addAttribute("addProductAdmins", addProductAdmins);

	    return "productlist";
	}

	@GetMapping("/productmoveinprogresspop/{id}")
	public String inProgress(@PathVariable Long id,Model model) {
		model.addAttribute("addProductAdmin", addProductAdminService.getAddProductAdminById(id));
		
		return "productmoveinprogresspop";
	}
	
	@PostMapping("/inProgressproduct/{id}")
	public String saveInprogressComments(@PathVariable Long id,
	                           @ModelAttribute("adminProductComments") AdminProductComments adminProductComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddProductAdmin entity
	    AddProductAdmin existingAddProductAdmin = addProductAdminService.getAddProductAdminById(id);
	    
	    ClientProduct existingClientProduct = clientProductService.getClientProductById(id);
	    existingAddProductAdmin.setStatus("In-Progress");
		   addProductAdminService.saveAddProductAdmin(existingAddProductAdmin);

	    // Create a new AdminProductComments
	    AdminProductComments comment = new AdminProductComments();
	    comment.setAddProductAdmin(existingAddProductAdmin);
	    comment.setClientProduct(existingClientProduct);
	    comment.setAdminProductComments(commentText);
	    comment.setAddedBy("Admin");
	    comment.setRequestStatus(true);
	    comment.setReason("Admin opted to change status to In-progress");
	    comment.setDateAdded(new Date());
	    comment.setTimeAdded(new Date());

	    // Set the productRequestId indirectly
	    comment.setProductRequestId(existingAddProductAdmin.getProductRequestId());

	    // Save the comment
	    adminProductCommentService.saveComment(comment);

	    // Handle requestStatus as needed

	    // Redirect to the Product view page
	    return "redirect:/viewproduct";
	}
	
	@GetMapping("/closeproductlistpop/{id}")
	public String closed(@PathVariable Long id,Model model) {
		model.addAttribute("addProductAdmin", addProductAdminService.getAddProductAdminById(id));
		  
		return "closeproductlistpop";
	}
	
	@PostMapping("/closepopproduct/{id}")
	public String saveCloseComments(@PathVariable Long id,
	                           @ModelAttribute("adminProductComments") AdminProductComments adminProductComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddProductAdmin entity
	    AddProductAdmin existingAddProductAdmin = addProductAdminService.getAddProductAdminById(id);
	    
	    ClientProduct existingClientProduct = clientProductService.getClientProductById(id);
	    existingAddProductAdmin.setStatus("Closed");
		   addProductAdminService.saveAddProductAdmin(existingAddProductAdmin);

	    // Create a new AdminProductComments
	    AdminProductComments comment = new AdminProductComments();
	    comment.setAddProductAdmin(existingAddProductAdmin);
	    comment.setClientProduct(existingClientProduct);
	    comment.setAdminProductComments(commentText);
	    comment.setAddedBy("Admin");
	    comment.setRequestStatus(true);
	    comment.setReason("Admin opted to close the support request");
	    comment.setDateAdded(new Date());
	    comment.setTimeAdded(new Date());

	    // Set the productRequestId indirectly
	    comment.setProductRequestId(existingAddProductAdmin.getProductRequestId());

	    // Save the comment
	    adminProductCommentService.saveComment(comment);

	    // Handle requestStatus as needed

	    // Redirect to the product view page
	    return "redirect:/viewproduct";
	}

	
	@GetMapping("/reopenproductpop/{id}")
	public String reopen(@PathVariable Long id,Model model) {
		model.addAttribute("addProductAdmin", addProductAdminService.getAddProductAdminById(id));
		
		return "reopenproductpop";
	}
	
	@PostMapping("/reOpenproducts/{id}")
	public String saveReopenComments(@PathVariable Long id,
	                           @ModelAttribute("adminProductComments") AdminProductComments adminProductComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddProductAdmin entity
	    AddProductAdmin existingAddProductAdmin = addProductAdminService.getAddProductAdminById(id);
	    
	    ClientProduct existingClientProduct = clientProductService.getClientProductById(id);
	    existingAddProductAdmin.setStatus("Re-Open");
		   addProductAdminService.saveAddProductAdmin(existingAddProductAdmin);

	    // Create a new AdminProductComments
	    AdminProductComments comment = new AdminProductComments();
	    comment.setAddProductAdmin(existingAddProductAdmin);
	    comment.setClientProduct(existingClientProduct);
	    comment.setAdminProductComments(commentText);
	    comment.setAddedBy("Admin");
	    comment.setRequestStatus(true);
	    comment.setReason("Admin opted to Re-open the support request");
	    comment.setDateAdded(new Date());
	    comment.setTimeAdded(new Date());

	    // Set the productRequestId indirectly
	    comment.setProductRequestId(existingAddProductAdmin.getProductRequestId());

	    // Save the comment
	    adminProductCommentService.saveComment(comment);

	    // Handle requestStatus as needed

	    // Redirect to the product view page
	    return "redirect:/viewproduct";
	}
	
	
	@GetMapping("/productcaputurepop/{id}")
	public String viewProductCommentsPop(@PathVariable Long id,Model model) {
		model.addAttribute("addProductAdmin", addProductAdminService.getAddProductAdminById(id));
		return "productcaputurepop";
	}
	
	@PostMapping("/saveCommentsproduct/{id}")
	public String saveComments(@PathVariable Long id,
	                           @ModelAttribute("adminProductComments") AdminProductComments adminProductComments,
	                           @RequestParam("commentText") String commentText,
	                           @RequestParam(value = "requestStatus", defaultValue = "false") boolean requestStatus,
	                           Model model) {

	    // Retrieve the AddProductAdmin entity
	    AddProductAdmin existingAddProductAdmin = addProductAdminService.getAddProductAdminById(id);
	    
	    ClientProduct existingClientProduct = clientProductService.getClientProductById(id);

	    // Create a new AdminProductComments
	    AdminProductComments comment = new AdminProductComments();
	    comment.setAddProductAdmin(existingAddProductAdmin);
	    comment.setClientProduct(existingClientProduct);
	    comment.setAdminProductComments(commentText);
	    comment.setAddedBy("Admin");
	    comment.setRequestStatus(requestStatus);
	    comment.setReason("Regular Comment");
	    comment.setDateAdded(new Date());
	    comment.setTimeAdded(new Date());

	    // Set the productRequestId indirectly
	    comment.setProductRequestId(existingAddProductAdmin.getProductRequestId());

	    // Save the comment
	    adminProductCommentService.saveComment(comment);

	    // Handle requestStatus as needed

	    // Redirect to the product view page
	    return "redirect:/viewproduct";
	}

	 private List<String> base64ImageList;
	 
	@GetMapping("/productsviewpop/{id}")
	public String viewProductComment(@PathVariable Long id, Model model) {
		 AddProductAdmin addProductAdmin = addProductAdminService.getAddProductAdminById(id);

	 	    // Get the list of SupportFiles associated with the AddSupportAdmin
	 	    List<ProductFiles> produtFilesList = addProductAdmin.getAdminProductFiles();
	 	    
	 	   List<AdminProductComments> adminProductCommentsList = addProductAdmin.getComments();

	 	    // Add the AddSupportAdmin and SupportFiles list to the model
	 	    model.addAttribute("addProductAdmin", addProductAdmin);

	        // Convert SupportFiles data to Base64 and add to the model
	        base64ImageList = new ArrayList<>();
	        for (ProductFiles productFile : produtFilesList) {
	            if (productFile.getFileData() != null) {
	                String base64Image = Base64.getEncoder().encodeToString(productFile.getFileData());
	                base64ImageList.add(base64Image);
	            } else {
	                base64ImageList.add(""); // or null, depending on your preference
	            }
	        }

	        model.addAttribute("addProductAdmin", addProductAdmin);
	        model.addAttribute("base64ImageList", base64ImageList);
	        model.addAttribute("adminProductCommentsList", adminProductCommentsList);
		return "viewproductdetails";
	}
	
	@GetMapping("/down/{id}")
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

	

	
	 @GetMapping("/deleteviewpop/{id}")
	    public String deleteProductAdmin(@PathVariable Long id) {
	        addProductAdminService.deleteAddProductAdminById(id);
	        clientProductService.deleteClientProductById(id);
	        return "redirect:/viewproduct";
	    }

}
