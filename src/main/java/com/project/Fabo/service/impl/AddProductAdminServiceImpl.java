package com.project.Fabo.service.impl;



import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import com.project.Fabo.entity.AddProductAdmin;
import com.project.Fabo.entity.ClientProduct;

import com.project.Fabo.repository.AddProductAdminRepository;
import com.project.Fabo.repository.ClientProductRepository;
import com.project.Fabo.service.AddProductAdminService;

@Service
public class AddProductAdminServiceImpl implements AddProductAdminService{
	
	private AddProductAdminRepository addProductAdminRepository;
	
	private ClientProductRepository clientProductRepository;

	public AddProductAdminServiceImpl(AddProductAdminRepository addProductAdminRepository,
			ClientProductRepository clientProductRepository) {
		this.addProductAdminRepository = addProductAdminRepository;
		this.clientProductRepository = clientProductRepository;
	}

	 @Value("${max.file.size}")
	 private long maxFileSize;

	@Override
	public AddProductAdmin saveAddProductAdmin(AddProductAdmin addProductAdmin) {
		 // Save the invoice to generate the auto-incremented numeric part
		AddProductAdmin savedaddProductAdmin = addProductAdminRepository.save(addProductAdmin);

        // Update the formatted invoice number based on the saved invoice ID
		savedaddProductAdmin.setFormattedProductNumber(savedaddProductAdmin.getId());
		return addProductAdminRepository.save(addProductAdmin);
		
	}

	@Override
	public AddProductAdmin getAddProductAdminById(Long id) {
			return addProductAdminRepository.findById(id).get();
	}
	
	public void saveCommentAndStatusById(Long id, String commentText, String clientVisible, String requestStatus) {
	    // Fetch the existing record by ID from the database
	    Optional<AddProductAdmin> productAdminOptional = addProductAdminRepository.findById(id);

	    // Check if the record exists
	    if (productAdminOptional.isPresent()) {
	        AddProductAdmin productAdmin = productAdminOptional.get();

	        // Update fields based on clientVisible and save to the database
	        if (clientVisible.equals("Yes")) {
	            productAdmin.setInternalComments(commentText);
	        } else {
	            productAdmin.setCommentsToClient(commentText);
	            productAdmin.setInternalComments(commentText);
	        }

	        productAdmin.setStatus(requestStatus);

	        // Save the updated productAdmin record back to the database
	        addProductAdminRepository.save(productAdmin);

	        // Now update the status in the clientProduct entity
	        Optional<ClientProduct> clientProductOptional = clientProductRepository.findById(id);
	        if (clientProductOptional.isPresent()) {
	            ClientProduct clientProduct = clientProductOptional.get();
	            clientProduct.setStatus(requestStatus);
	            clientProduct.setExternalComments(commentText);
	            clientProductRepository.save(clientProduct); // Save the updated clientProduct entity
	        } else {
	            // Handle case when the clientSupport ID doesn't exist
	            // You might throw an exception or handle it according to your application logic
	            throw new IllegalArgumentException("Client Product ID not found: " + id);
	        }
	    } else {
	        // Handle case when the supportAdmin ID doesn't exist
	        // You might throw an exception or handle it according to your application logic
	        throw new IllegalArgumentException("Product Admin ID not found: " + id);
	    }
	}


	 public void deleteAddProductAdminById(Long id) {
	        Optional<AddProductAdmin> productAdminOptional = addProductAdminRepository.findById(id);

	        if (productAdminOptional.isPresent()) {
	            AddProductAdmin productAdmin = productAdminOptional.get();
	            productAdmin.setActiveStatus(false); // Marking as inactive

	            addProductAdminRepository.save(productAdmin);
	        } else {
	            throw new IllegalArgumentException("Product Admin ID not found: " + id);
	        }
	    }

}

