package com.project.Fabo.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.Fabo.entity.AddSupportAdmin;
import com.project.Fabo.entity.ClientSupport;
import com.project.Fabo.repository.AddSupportAdminRepository;
import com.project.Fabo.repository.ClientSupportRepository;
import com.project.Fabo.service.AddSupportAdminService;

@Service
public class AddSupportAdminServiceImpl implements AddSupportAdminService{
	
	private AddSupportAdminRepository addSupportAdminRepository;
	
	private ClientSupportRepository clientSupportRepository;

	public AddSupportAdminServiceImpl(AddSupportAdminRepository addSupportAdminRepository,
			ClientSupportRepository clientSupportRepository) {
		this.addSupportAdminRepository = addSupportAdminRepository;
		this.clientSupportRepository = clientSupportRepository;
	}

	 @Value("${max.file.size}")
	 private long maxFileSize;

	@Override
	public AddSupportAdmin saveAddSupportAdmin(AddSupportAdmin addSupportAdmin) {
		 // Save the invoice to generate the auto-incremented numeric part
		AddSupportAdmin savedaddSupportAdmin = addSupportAdminRepository.save(addSupportAdmin);

        // Update the formatted invoice number based on the saved invoice ID
		savedaddSupportAdmin.setFormattedSupportNumber(savedaddSupportAdmin.getId());
		return addSupportAdminRepository.save(addSupportAdmin);
		
	}

	@Override
	public AddSupportAdmin getAddSupportAdminById(Long id) {
			return addSupportAdminRepository.findById(id).get();
	}
	
	public void saveCommentAndStatusById(Long id, String commentText, String clientVisible, String requestStatus) {
	    // Fetch the existing record by ID from the database
	    Optional<AddSupportAdmin> supportAdminOptional = addSupportAdminRepository.findById(id);

	    // Check if the record exists
	    if (supportAdminOptional.isPresent()) {
	        AddSupportAdmin supportAdmin = supportAdminOptional.get();

	        // Update fields based on clientVisible and save to the database
	        if (clientVisible.equals("Yes")) {
	            supportAdmin.setInternalComments(commentText);
	        } else {
	            supportAdmin.setCommentsToClient(commentText);
	            supportAdmin.setInternalComments(commentText);
	        }

	        supportAdmin.setStatus(requestStatus);

	        // Save the updated supportAdmin record back to the database
	        addSupportAdminRepository.save(supportAdmin);

	        // Now update the status in the clientSupport entity
	        Optional<ClientSupport> clientSupportOptional = clientSupportRepository.findById(id);
	        if (clientSupportOptional.isPresent()) {
	            ClientSupport clientSupport = clientSupportOptional.get();
	            clientSupport.setStatus(requestStatus);
	            clientSupport.setExternalComments(commentText);
	            clientSupportRepository.save(clientSupport); // Save the updated clientSupport entity
	        } else {
	            // Handle case when the clientSupport ID doesn't exist
	            // You might throw an exception or handle it according to your application logic
	            throw new IllegalArgumentException("Client Support ID not found: " + id);
	        }
	    } else {
	        // Handle case when the supportAdmin ID doesn't exist
	        // You might throw an exception or handle it according to your application logic
	        throw new IllegalArgumentException("Support Admin ID not found: " + id);
	    }
	}
	
	public void deleteAddSupportAdminById(Long id) {
	    Optional<AddSupportAdmin> supportAdminOptional = addSupportAdminRepository.findById(id);

	    if (supportAdminOptional.isPresent()) {
	        AddSupportAdmin supportAdmin = supportAdminOptional.get();
	        supportAdmin.setActiveStatus(false); // Marking as inactive

	        addSupportAdminRepository.save(supportAdmin);
	    } else {
	        throw new IllegalArgumentException("Add Support Admin ID not found: " + id);
	    }
	}


}
