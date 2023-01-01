package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title","Home - Smart Contect Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title","About - Smart Contect Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title","Register - Smart Contect Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@RequestMapping("/login")
	public String login(Model model) {
		
		model.addAttribute("title","sign in - Smart Contect Manager");
		return "login";
	}
	@RequestMapping(value="/do_register" ,method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result,@RequestParam(value="agreement" ,defaultValue = "false") boolean agreement ,Model model,HttpSession session) {
		
		try { 
			if(!agreement) {
				System.out.println("It is must to agree the term and conditions to proceed further..Pls check the box.");
				throw new Exception("It is must to agree the term and conditions to proceed further..Pls check the box.");
			}
			if(result.hasErrors()) {
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER"); 
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User res  = this.userRepository.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("sucessfully Registered !!","alert-success"));
			return "signup";
		} catch (Exception e) {
			// TODO: handle exception
			e.getStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("something went wrong !! you have not agree to term and conditions","alert-danger"));
			return "signup";
		}
	}
}
