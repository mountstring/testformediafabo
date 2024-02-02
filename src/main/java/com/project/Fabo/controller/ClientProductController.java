package com.project.Fabo.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
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

import com.project.Fabo.entity.AddProductAdmin;
import com.project.Fabo.entity.AdminProductComments;
import com.project.Fabo.entity.Client;
import com.project.Fabo.entity.ClientProduct;
import com.project.Fabo.entity.ProductFiles;
import com.project.Fabo.repository.ClientProductRepository;
import com.project.Fabo.repository.ClientRepository;
import com.project.Fabo.service.AddProductAdminService;
import com.project.Fabo.service.AdminProductCommentService;
import com.project.Fabo.service.ClientProductService;
import com.project.Fabo.service.ProductMirrorService;
import com.project.Fabo.service.UserService;



@Controller
public class ClientProductController {
	
	private ClientProductService clientProductService;
	
	private ClientRepository clientRepository;
	
	private ClientProductRepository clientProductRepository;
	
	private ProductMirrorService productMirrorService;
	
	private AddProductAdminService addProductAdminService;
	
	private AdminProductCommentService adminProductCommentService;
	
	private UserService userService;


	public ClientProductController(ClientProductService clientProductService, ClientRepository clientRepository,
			ClientProductRepository clientProductRepository, ProductMirrorService productMirrorService,
			AddProductAdminService addProductAdminService, AdminProductCommentService adminProductCommentService,
			UserService userService, List<String> base64ImageList) {
		super();
		this.clientProductService = clientProductService;
		this.clientRepository = clientRepository;
		this.clientProductRepository = clientProductRepository;
		this.productMirrorService = productMirrorService;
		this.addProductAdminService = addProductAdminService;
		this.adminProductCommentService = adminProductCommentService;
		this.userService = userService;
		this.base64ImageList = base64ImageList;
	}

	@GetMapping("/clientaddproduct")
	public String createProductByClient(Model model) { 
		List<Client> clients;
		clients = clientRepository.findAll();
		 ClientProduct clientProduct = new ClientProduct();
		model.addAttribute("clientProduct",clientProduct);
		 model.addAttribute("clients", clients);
		return "clientaddproduct"; 
	}
	
	@PostMapping("/clientproductviewproduct")
	public String saveClientProduct(@ModelAttribute("clientProduct") ClientProduct clientProduct,
			@RequestParam("storeName") String storeName,
			 @RequestParam(value = "files", required = false)  List<MultipartFile> files) {
		clientProduct.setStatus("New");
		clientProduct.setCommentsToAdmin("No Comments");
		clientProduct.setExternalComments("No Comments");
		
		clientProductService.saveClientProduct(clientProduct);
		productMirrorService.addProductRecordToBothSide(clientProduct);
		  AddProductAdmin addProductAdmin = addProductAdminService.getAddProductAdminById(clientProduct.getId());
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
	        clientProduct.setAdminProductFiles(adminProductFiles);
		clientProductService.saveClientProduct(clientProduct);
		
		
		return "redirect:/clientproductview";
	}
	
	/*@GetMapping("/clientproductview")
	public String filtersWithList(
	    @RequestParam(value = "productRequestType", required = false) String productRequesttype,
	    @RequestParam(value = "statusDropdown", required = false) String statusDropdown,
	    @RequestParam(value = "searchTerm", required = false) String searchTerm,
	    Model model
	) {
	    List<ClientProduct> clientProducts;

	    if (searchTerm != null && !searchTerm.isEmpty()) {
	        // Filter by search term across multiple fields
	        clientProducts = clientProductRepository.findBySearchTerm(searchTerm);
	        model.addAttribute("searchTerm", searchTerm);
	    } else if (productRequesttype != null && !productRequesttype.isEmpty()) {
	        // Filter by invoice dropdown
	        if ("All".equalsIgnoreCase(productRequesttype)) {
	            clientProducts = clientProductRepository.findByActiveStatusTrue();
	        } else {
	            clientProducts = clientProductRepository.findByProductRequestTypeContaining(productRequesttype);
	        }
	        model.addAttribute("selectedType", productRequesttype);
	    } else if (statusDropdown != null && !statusDropdown.isEmpty()) {
	        // Filter by status dropdown
	        if ("All".equalsIgnoreCase(statusDropdown)) {
	            clientProducts = clientProductRepository.findByActiveStatusTrue();
	        } else {
	            clientProducts = clientProductRepository.findByStatus(statusDropdown);
	        }
	        model.addAttribute("selected", statusDropdown);
	    } else {
	        // No filters applied, return all records
	        clientProducts = clientProductRepository.findByActiveStatusTrue();
	    }
	    model.addAttribute("clientProducts", clientProducts);
	    return "clientproductlist";
	}*/
	
	@GetMapping("/clientproductview")
	public String filtersWithList(
	    @RequestParam(value = "productRequestType", required = false) String productRequesttype,
	    @RequestParam(value = "statusDropdown", required = false) String statusDropdown,
	    @RequestParam(value = "searchTerm", required = false) String searchTerm,
	    Model model,
	    Principal principal // Add Principal parameter to retrieve logged-in user information
	) {
	    String username = principal.getName(); // Retrieve the currently logged-in user's email

	    // Retrieve the user's store code based on their email
	    String userStoreCode = userService.getUserStoreCodeByUserName(username); // Replace userService with your actual service

	    List<ClientProduct> clientProducts;

	    if (searchTerm != null && !searchTerm.isEmpty()) {
	        // Filter by search term across multiple fields and store code
	        clientProducts = clientProductRepository.findBySearchTermAndStoreCode(searchTerm, userStoreCode);
	        model.addAttribute("searchTerm", searchTerm);
	    } else if (productRequesttype != null && !productRequesttype.isEmpty()) {
	        // Filter by product request type and store code
	        if ("All".equalsIgnoreCase(productRequesttype)) {
	            clientProducts = clientProductRepository.findByActiveStatusAndStoreCode(true, userStoreCode);
	        } else {
	            clientProducts = clientProductRepository.findByProductRequestTypeContainingAndStoreCode(productRequesttype, userStoreCode);
	        }
	        model.addAttribute("selectedType", productRequesttype);
	    } else if (statusDropdown != null && !statusDropdown.isEmpty()) {
	        // Filter by status dropdown and store code
	        if ("All".equalsIgnoreCase(statusDropdown)) {
	            clientProducts = clientProductRepository.findByActiveStatusAndStoreCode(true, userStoreCode);
	        } else {
	            clientProducts = clientProductRepository.findByStatusAndStoreCode(statusDropdown, userStoreCode);
	        }
	        model.addAttribute("selected", statusDropdown);
	    } else {
	        // No filters applied, return all records for the user's store code
	        clientProducts = clientProductRepository.findByActiveStatusAndStoreCode(true, userStoreCode);
	    }
	    model.addAttribute("clientProducts", clientProducts);
	    return "clientproductlist";
	}


	@GetMapping("/viewproductclientproduct/{id}")
	public String viewProductdetails(@PathVariable Long id, Model model) {
		model.addAttribute("clientProduct", clientProductService.getClientProductById(id));
		return "clientproductcapturecompop";
	}
	
	@PostMapping("/saveCommentproduct/{id}")
	public String saveComments(@PathVariable Long id,
	                           @ModelAttribute("adminProductComments") AdminProductComments adminProductComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddProductAdmin entity
	    AddProductAdmin existingAddProductAdmin = addProductAdminService.getAddProductAdminById(id);
	    
	    ClientProduct existingClientProduct = clientProductService.getClientProductById(id);

	    // Create a new AdminComments
	    AdminProductComments comment = new AdminProductComments();
	    comment.setAddProductAdmin(existingAddProductAdmin);
	    comment.setClientProduct(existingClientProduct);
	    comment.setAdminProductComments(commentText);
	    comment.setAddedBy("Client");
	    comment.setRequestStatus(true); // Set "yes" if true, "no" otherwise
	    comment.setReason("Regular Comment");
	    comment.setDateAdded(new Date());
	    comment.setTimeAdded(new Date());

	    // Set the productRequestId indirectly
	    comment.setProductRequestId(existingClientProduct.getProductRequestId());

	    // Save the comment
	    adminProductCommentService.saveComment(comment);

	    // Redirect to the support view page
	    return "redirect:/clientproductview";
	}
	
	@GetMapping("/closeproduct/{id}")
	public String closed(@PathVariable Long id,Model model) {
		model.addAttribute("clientProduct", clientProductService.getClientProductById(id));
		
		return "closeproductreqpop";
	}
	
	@PostMapping("/closeproductrequestproduct/{id}")
	public String saveCloseComments(@PathVariable Long id,
	                           @ModelAttribute("adminProductComments") AdminProductComments adminProductComments,
	                           @RequestParam("commentText") String commentText,
	                           Model model) {

	    // Retrieve the AddProductAdmin entity
	    AddProductAdmin existingAddProductAdmin = addProductAdminService.getAddProductAdminById(id);
	    
	    ClientProduct existingClientProduct = clientProductService.getClientProductById(id);
		existingClientProduct.setStatus("Closed");
		   clientProductService.saveClientProduct(existingClientProduct);


	    // Create a new AdminProductComments
	    AdminProductComments comment = new AdminProductComments();
	    comment.setAddProductAdmin(existingAddProductAdmin);
	    comment.setClientProduct(existingClientProduct);
	    comment.setAddedBy("Client");
	    comment.setAdminProductComments(commentText);
	    comment.setRequestStatus(true);
	    comment.setReason("client opted to close the product request");
	    comment.setDateAdded(new Date());
	    comment.setTimeAdded(new Date());

	    // Set the productRequestId indirectly
	    comment.setProductRequestId(existingAddProductAdmin.getProductRequestId());
	    
	    // Save the comment
	    adminProductCommentService.saveComment(comment);

	    // Handle requestStatus as needed

	    // Redirect to the Product view page
	    return "redirect:/clientproductview";
	}

	private List<String> base64ImageList; // Declare it as a class field

    @GetMapping("/clientproductDetailsproduct/{id}")
    public String viewProductDetails(@PathVariable Long id, Model model) {
        // Your existing code to populate addProductAdmin and ProductFilesList
    	 ClientProduct clientProduct = clientProductService.getClientProductById(id);

 	    // Get the list of ProductFiles associated with the AddProductAdmin
 	    List<ProductFiles> productFilesList = clientProduct.getAdminProductFiles();
 	    
 	   List<AdminProductComments> adminProductCommentsList = clientProduct.getComments().stream()
 		        .filter(AdminProductComments::getRequestStatus) // Filter by getRequestStatus = true
 		        .collect(Collectors.toList());


 	    // Add the AddProductAdmin and ProductFiles list to the model
 	    model.addAttribute("ClientProduct", clientProduct);

        // Convert ProductFiles data to Base64 and add to the model
        base64ImageList = new ArrayList<>();
        for (ProductFiles productFile : productFilesList) {
            if (productFile.getFileData() != null) {
                String base64Image = Base64.getEncoder().encodeToString(productFile.getFileData());
                base64ImageList.add(base64Image);
            } else {
                base64ImageList.add(""); // or null, depending on your preference
            }
        }

        model.addAttribute("clientProduct", clientProduct);
        model.addAttribute("base64ImageList", base64ImageList);
        model.addAttribute("adminProductCommentsList", adminProductCommentsList);

        return "clientviewproductdts";
    }

    @GetMapping("/downloadsproduct/{id}")
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
