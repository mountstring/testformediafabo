package com.project.Fabo.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.Fabo.entity.AddSupportAdmin;
import com.project.Fabo.entity.ClientSupport;
import com.project.Fabo.repository.AddSupportAdminRepository;
import com.project.Fabo.repository.ClientSupportRepository;
import com.project.Fabo.service.ClientSupportService;

@Service
public class ClientSupportServiceImpl implements ClientSupportService{
	
	private ClientSupportRepository clientSupportRepository;
	
	private AddSupportAdminRepository addSupportAdminRepository;

	public ClientSupportServiceImpl(ClientSupportRepository clientSupportRepository,
			AddSupportAdminRepository addSupportAdminRepository) {
		this.clientSupportRepository = clientSupportRepository;
		this.addSupportAdminRepository = addSupportAdminRepository;
	}

	@Value("${max.file.size}") // Define this property in your application.properties or application.yml
	    private long maxFileSize;

	@Override
	public ClientSupport saveClientSupport(ClientSupport clientSupport) {
		ClientSupport savedclientSupport = clientSupportRepository.save(clientSupport);
		savedclientSupport.setFormattedSupportNumber(savedclientSupport.getId());
		return clientSupportRepository.save(clientSupport);
	}

	@Override
	public ClientSupport getClientSupportById(Long id) {
		return clientSupportRepository.findById(id).get();
	}

	@Override
	public ClientSupport updateAdmin(ClientSupport existingClientSupport) {
		return clientSupportRepository.save(existingClientSupport);
	}

	 @Override
	    public void saveComment(Long id, String comment) {
	        // Retrieve client support entity by ID
	        Optional<ClientSupport> optionalClientSupport = clientSupportRepository.findById(id);
	        Optional<AddSupportAdmin> optionalAddSupportAdmin = addSupportAdminRepository.findById(id);

	        // Check if the entity exists and save the comment
	        optionalClientSupport.ifPresent(clientSupport -> {
	            clientSupport.setCommentsToAdmin(comment);
	            clientSupportRepository.save(clientSupport);
	        });
	        
	        optionalAddSupportAdmin.ifPresent(addSupportAdmin -> {
	            addSupportAdmin.setCommentsFromClient(comment);
	            addSupportAdminRepository.save(addSupportAdmin);
	        });
	    }

	@Override
	public void saveCloseComment(Long id, String closeComment, String close) {
		
		 Optional<ClientSupport> optionalClientSupport = clientSupportRepository.findById(id);
	        Optional<AddSupportAdmin> optionalAddSupportAdmin = addSupportAdminRepository.findById(id);

	        // Check if the entity exists and save the comment
	        optionalClientSupport.ifPresent(clientSupport -> {
	            clientSupport.setCommentsToAdmin(closeComment);
	            clientSupport.setStatus(close);
	            clientSupportRepository.save(clientSupport);
	        });
	        
	        optionalAddSupportAdmin.ifPresent(addSupportAdmin -> {
	            addSupportAdmin.setCommentsFromClient(closeComment);
	            addSupportAdmin.setStatus(close);
	            addSupportAdminRepository.save(addSupportAdmin);
	        });
		
	}


	public void deleteClientSuppportById(Long id) {
	    Optional<ClientSupport> clientsupportOptional = clientSupportRepository.findById(id);

	    if (clientsupportOptional.isPresent()) {
	        ClientSupport clientSupport = clientsupportOptional.get();
	        clientSupport.setActiveStatus(false); // Marking as inactive

	        clientSupportRepository.save(clientSupport);
	    } else {
	        throw new IllegalArgumentException("Client Support ID not found: " + id);
	    }
	}


}
