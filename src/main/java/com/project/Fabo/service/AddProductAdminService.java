package com.project.Fabo.service;


import com.project.Fabo.entity.AddProductAdmin;



public interface AddProductAdminService {

	AddProductAdmin saveAddProductAdmin(AddProductAdmin addProductAdmin);

	AddProductAdmin getAddProductAdminById(Long id);

	void saveCommentAndStatusById(Long id, String commentText, String clientVisible, String requestStatus);

	void deleteAddProductAdminById(Long id);

}
 