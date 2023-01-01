package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
        String username = principal.getName();	
		User user = userRepository.gerUserByUserName(username);
		model.addAttribute("title","Dashboard - Smart Contect Manager");
		model.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","user Dashboard - Smart Contect Manager");
		return "normal/user_dashboard";
	}
	@RequestMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add Contact - Smart Contect Manager");
		model.addAttribute("contact" , new Contact());
		return "normal/add_contact_form";
	}
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
		
		try {
			String name = principal.getName();
			User user = this.userRepository.gerUserByUserName(name);
			
			if(file.isEmpty()) {
				contact.setImage("default.png");
			}
			else {
				contact.setImage(file.getOriginalFilename() );
				File file2 = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your Contact Added !! add more..","sucess"));
			
		} catch (Exception e) {
			System.out.println("ERROR"+e.getMessage());
			session.setAttribute("message", new Message("Something went wrong  !! Try Again..","danger"));
		}
		return "normal/add_contact_form";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model, Principal principal) {
		model.addAttribute("title","Show Contact - Smart Contect Manager");
		String userName =principal.getName();
		User user = this.userRepository.gerUserByUserName(userName);
		
		PageRequest pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts",contacts);   
		model.addAttribute("currentPages",page);   
		model.addAttribute("totalPages",contacts.getTotalPages());   
		return "normal/show_contacts";
	}
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		String userName = principal.getName();
		User user = this.userRepository.gerUserByUserName(userName);
		Contact contact = contactOptional.get();
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact" , contact);
			model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_details";
	}
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId ,HttpSession session) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		 
		this.contactRepository.delete(contact);
		session.setAttribute("message",new Message("Contact deleted sucessfully..","sucess"));
		return "redirect:/user/show-contacts/0";
	}
	
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId,Model model) {
		model.addAttribute("title","Update Contact");
		
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	@RequestMapping(value="/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal) {
		
		try {
			
			Contact oldContact = this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//delete old photo
				File deleteFile = new ClassPathResource("static/images").getFile();
				File file1 = new File(deleteFile,oldContact.getImage());
				file1.delete(); 
				
				//update new photo
				File file2 = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			   contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContact.getImage());
			}
			User user = this.userRepository.gerUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact is Updated..","sucess")); 
			 
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
}
