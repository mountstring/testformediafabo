package com.project.Fabo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Fabo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByUserName(String userName);

	List<User> findAllByUserName(String email);

	void deleteAllByUserName(String email);

	void deleteByUserName(String email);

	User findByPassword(String username);




}
