package com.project.Fabo.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.Fabo.entity.Invoice;
import com.project.Fabo.repository.InvoiceRepository;
import com.project.Fabo.service.InvoiceService;

@Service
public class InvoiceServiceImpl implements InvoiceService{
	
	@Autowired
	private InvoiceRepository invoiceRepository;
	
	public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
		this.invoiceRepository = invoiceRepository;
	}

	 @Override
	    public List<Invoice> getAllInvoices() {
	        return invoiceRepository.findAll();
	    }

	 public Invoice saveInvoice(Invoice invoice) {
	        // Save the invoice to generate the auto-incremented numeric part
	        Invoice savedInvoice = invoiceRepository.save(invoice);

	        // Update the formatted invoice number based on the saved invoice ID
	        savedInvoice.setFormattedInvoiceNumber(savedInvoice.getId());

	        // Save the invoice again to update the invoice number
	        return invoiceRepository.save(savedInvoice);
	    }

		@Override
		public Invoice getInvoiceById(Long id) {
			return invoiceRepository.findById(id).get();
		}

		@Override
		public Invoice updateInvoice(Invoice invoice) {
			return invoiceRepository.save(invoice);
		}
		
		public void deleteInvoiceById(Long id) {
	        Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);

	        if (invoiceOptional.isPresent()) {
	            Invoice invoice = invoiceOptional.get();
	            invoice.setActiveStatus(false); // Marking as inactive

	            invoiceRepository.save(invoice);
	        } else {
	            throw new IllegalArgumentException("Invoice ID not found: " + id);
	        }
	    }
		
	   

}
