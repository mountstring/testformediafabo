package com.project.Fabo.service.impl;

import org.springframework.stereotype.Service;

import com.project.Fabo.entity.AdminComments;
import com.project.Fabo.repository.AdminCommentsRepository;
import com.project.Fabo.service.AdminCommentService;
@Service
public class AdminCommentsServiceImpl implements AdminCommentService{
	
	private AdminCommentsRepository adminCommentsRepository;

	public AdminCommentsServiceImpl(AdminCommentsRepository adminCommentsRepository) {
		super();
		this.adminCommentsRepository = adminCommentsRepository;
	}

	@Override
	public AdminComments saveComment(AdminComments comment) {
		return adminCommentsRepository.save(comment);
	}

}
