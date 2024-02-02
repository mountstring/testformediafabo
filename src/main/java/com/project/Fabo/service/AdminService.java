package com.project.Fabo.service;

import java.util.List;

import com.project.Fabo.entity.Admin;

public interface AdminService {
	
	List<Admin> getAllAdmins();

	Admin saveAdmin(Admin admin);

	Admin getAdminById(String userName);

	Admin updateAdmin(Admin Admin);

	void deleteAdminByUserName(String userName);

	void addAdminAndRoles(Admin admin, List<Long> roleIds);

	void removeAdminAndAssociationsByUserName(String userName);

	void updateConcatenatedRolesByEmail(String userName, String concatenatedRoleNames);

	boolean isUsernameDuplicate(String username);



}
