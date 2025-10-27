package com.example.ecoswap.controller;

import com.example.ecoswap.model.*;
import com.example.ecoswap.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

    private final UserRepository userRepo;
    private final SellerProfileRepository sellerRepo;
    private final CustomerProfileRepository customerRepo;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepo,
                                  SellerProfileRepository sellerRepo,
                                  CustomerProfileRepository customerRepo,
                                  PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.sellerRepo = sellerRepo;
        this.customerRepo = customerRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // show forms
    @GetMapping("/register-customer")
    public String showCustomerForm(Model model) {
        model.addAttribute("customer", new CustomerRegistrationForm());
        return "auth/register-customer";
    }

    @GetMapping("/register-seller")
    public String showSellerForm(Model model) {
        model.addAttribute("seller", new SellerRegistrationForm());
        return "auth/register-seller";
    }

    // post handlers
    @PostMapping("/register-customer")
    public String registerCustomer(@ModelAttribute CustomerRegistrationForm form, BindingResult br, Model model) {
        if (userRepo.findByEmail(form.getEmail()).isPresent()) {
            model.addAttribute("error", "Email already registered");
            return "auth/register-customer";
        }

        User u = new User(form.getEmail(), passwordEncoder.encode(form.getPassword()), "ROLE_CUSTOMER");
        userRepo.save(u);
        CustomerProfile cp = new CustomerProfile(u, form.getFullName(), form.getAddress(), form.getPhone());
        customerRepo.save(cp);
        return "redirect:/login";
    }

    @PostMapping("/register-seller")
    public String registerSeller(@ModelAttribute SellerRegistrationForm form, BindingResult br, Model model) {
        if (userRepo.findByEmail(form.getEmail()).isPresent()) {
            model.addAttribute("error", "Email already registered");
            return "auth/register-seller";
        }

        User u = new User(form.getEmail(), passwordEncoder.encode(form.getPassword()), "ROLE_SELLER");
        userRepo.save(u);
        SellerProfile sp = new SellerProfile(u, form.getStoreName(), form.getBusinessAddress());
        sellerRepo.save(sp);
        return "redirect:/login";
    }

    // registration form DTOs as inner static classes (simple)
    public static class CustomerRegistrationForm {
        private String email;
        private String password;
        private String fullName;
        private String address;
        private String phone;
        // getters & setters ...
        public String getEmail(){return email;} public void setEmail(String e){this.email=e;}
        public String getPassword(){return password;} public void setPassword(String p){this.password=p;}
        public String getFullName(){return fullName;} public void setFullName(String n){this.fullName=n;}
        public String getAddress(){return address;} public void setAddress(String a){this.address=a;}
        public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    }

    public static class SellerRegistrationForm {
        private String email;
        private String password;
        private String storeName;
        private String businessAddress;
        // getters & setters ...
        public String getEmail(){return email;} public void setEmail(String e){this.email=e;}
        public String getPassword(){return password;} public void setPassword(String p){this.password=p;}
        public String getStoreName(){return storeName;} public void setStoreName(String s){this.storeName=s;}
        public String getBusinessAddress(){return businessAddress;} public void setBusinessAddress(String b){this.businessAddress=b;}
    }
}
