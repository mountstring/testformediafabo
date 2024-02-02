package com.project.Fabo.service;

import java.util.List;

public interface RolesTableService {

	String getConcatenatedRoleNamesByEmail(String email, List<Long> roleIds);

	

}
