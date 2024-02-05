package com.project.Fabo.service;


import com.project.Fabo.entity.AddSupportAdmin;


public interface AddSupportAdminService {

	AddSupportAdmin saveAddSupportAdmin(AddSupportAdmin addSupportAdmin);

	AddSupportAdmin getAddSupportAdminById(Long id);

	void saveCommentAndStatusById(Long id, String commentText, String clientVisible, String requestStatus);

	void deleteAddSupportAdminById(Long id);

}
