package com.project.Fabo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.Fabo.entity.Invoice;

@Service
public interface InvoiceService {

List<Invoice> getAllInvoices();
	
	Invoice saveInvoice(Invoice Invoice);
	
	Invoice getInvoiceById(Long id);
	
	Invoice updateInvoice(Invoice Invoice);
	
	void deleteInvoiceById(Long id);

}
