package com.project.Fabo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.Fabo.entity.AddSupportAdmin;

public interface AddSupportAdminRepository extends JpaRepository<AddSupportAdmin, Long>{
	 List<AddSupportAdmin> findByActiveStatusTrue();

	List<AddSupportAdmin> findBySupportRequestTypeContaining(String invoicedropdown);

	List<AddSupportAdmin> findByStatus(String statusDropdown);

	List<AddSupportAdmin> findBySupportRequestTypeContainingAndStatus(String invoicedropdown, String statusDropdown);
	
	@Query("SELECT a FROM AddSupportAdmin a WHERE " +
	        "LOWER(a.storeName) LIKE %:searchTerm% OR " +
	        "LOWER(a.storeCode) LIKE %:searchTerm% OR " +
	        "LOWER(a.supportRequestType) LIKE %:searchTerm% OR " +
	        "LOWER(a.storeContact) LIKE %:searchTerm% OR " +
	        "LOWER(a.issueSubject) LIKE %:searchTerm% OR " +
	        "LOWER(a.issueDescription) LIKE %:searchTerm% OR " +
	        "LOWER(a.status) LIKE %:searchTerm% OR " +
	        "LOWER(a.internalComments) LIKE %:searchTerm% OR " +
	        "LOWER(a.commentsToClient) LIKE %:searchTerm% OR " +
	        "LOWER(a.commentsFromClient) LIKE %:searchTerm%")
	List<AddSupportAdmin> findBySearchTerm(@Param("searchTerm") String searchTerm);
	
	@Query("SELECT DISTINCT a.storeCode FROM AddSupportAdmin a WHERE a.activeStatus = true")
	List<String> findDistinctStoreCodes();

	
}
