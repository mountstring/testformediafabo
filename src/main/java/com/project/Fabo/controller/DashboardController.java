package com.project.Fabo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
	
	@GetMapping("/superadmin.dashboard")
	public String superadmin() {
		return "superadmin.dashboard";
	}
	
	@GetMapping("/supportadmindashboard")
	public String superadmin1() {
		return "supportadmindashboard";
	}
	
	@GetMapping("/accountadmindashboard")
	public String superadmin2() {
		return "accountadmindashboard";
	}
	
	@GetMapping("/productadmindashboard")
	public String superadmin3() {
		return "productadmindashboard";
	}
	
	@GetMapping("/clientsupportadmindash")
	public String superadmin4() {
		return "clientsupportadmindash";
	}

	@GetMapping("/clientaccountadmindash")
	public String superadmin5() {
		return "clientaccountadmindash";
	}

	@GetMapping("/clientproductadmindash")
	public String superadmin6() {
		return "clientproductadmindash";
	}


}
