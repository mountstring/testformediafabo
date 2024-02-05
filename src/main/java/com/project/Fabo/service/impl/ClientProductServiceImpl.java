package com.project.Fabo.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.Fabo.entity.AddProductAdmin;
import com.project.Fabo.entity.ClientProduct;
import com.project.Fabo.repository.AddProductAdminRepository;
import com.project.Fabo.repository.ClientProductRepository;
import com.project.Fabo.service.ClientProductService;


@Service
public class ClientProductServiceImpl implements ClientProductService{
	
	private ClientProductRepository clientProductRepository;
	
	private AddProductAdminRepository addProductAdminRepository;

	public ClientProductServiceImpl(ClientProductRepository clientProductRepository,
			AddProductAdminRepository addProductAdminRepository) {
		this.clientProductRepository = clientProductRepository;
		this.addProductAdminRepository = addProductAdminRepository;
	}

	@Value("${max.file.size}") // Define this property in your application.properties or application.yml
	    private long maxFileSize;



	@Override
	public ClientProduct saveClientProduct(ClientProduct clientProduct) {
		ClientProduct savedclientProduct = clientProductRepository.save(clientProduct);
		return clientProductRepository.save(clientProduct);
	}

	@Override
	public ClientProduct getClientProductById(Long id) {
		return clientProductRepository.findById(id).get();
	}

	@Override
	public ClientProduct updateAdmin(ClientProduct existingClientProduct) {
		return clientProductRepository.save(existingClientProduct);
	}

	 @Override
	    public void saveComment(Long id, String comment) {
	        // Retrieve client support entity by ID
	        Optional<ClientProduct> optionalClientProduct = clientProductRepository.findById(id);
	        Optional<AddProductAdmin> optionalAddProductAdmin = addProductAdminRepository.findById(id);

	        // Check if the entity exists and save the comment
	        optionalClientProduct.ifPresent(clientProduct -> {
	            clientProduct.setCommentsToAdmin(comment);
	            clientProductRepository.save(clientProduct);
	        });
	        
	        optionalAddProductAdmin.ifPresent(addProductAdmin -> {
	            addProductAdmin.setCommentsFromClient(comment);
	            addProductAdminRepository.save(addProductAdmin);
	        });
	    }

	@Override
	public void saveCloseComment(Long id, String closeComment, String close) {
		
		 Optional<ClientProduct> optionalClientProduct = clientProductRepository.findById(id);
	        Optional<AddProductAdmin> optionalAddProductAdmin = addProductAdminRepository.findById(id);

	        // Check if the entity exists and save the comment
	        optionalClientProduct.ifPresent(clientProduct -> {
	            clientProduct.setCommentsToAdmin(closeComment);
	            clientProduct.setStatus(close);
	            clientProductRepository.save(clientProduct);
	        });
	        
	        optionalAddProductAdmin.ifPresent(addProductAdmin -> {
	            addProductAdmin.setCommentsFromClient(closeComment);
	            addProductAdmin.setStatus(close);
	            addProductAdminRepository.save(addProductAdmin);
	        });
		
	}


	public void deleteClientProductById(Long id) {
	    Optional<ClientProduct> clientproductOptional = clientProductRepository.findById(id);

	    if (clientproductOptional.isPresent()) {
	        ClientProduct clientProduct = clientproductOptional.get();
	        clientProduct.setActiveStatus(false); // Marking as inactive

	        clientProductRepository.save(clientProduct);
	    } else {
	        throw new IllegalArgumentException("Client Product ID not found: " + id);
	    }
	}



}
