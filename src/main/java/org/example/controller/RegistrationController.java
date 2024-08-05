package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.Entity.User;
import org.example.Entity.VerificationToken;
import org.example.dto.UserDTO;
import org.example.event.RegistrationEvent;
import org.example.repository.VerificationTokenRepository;
import org.example.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/register")
public class RegistrationController {

    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final VerificationTokenRepository verificationTokenRepository;

    public RegistrationController(UserService userService, ApplicationEventPublisher applicationEventPublisher, VerificationTokenRepository verificationTokenRepository) {
        this.userService = userService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @PostMapping
    public ResponseEntity<Object> registerUser(@RequestBody UserDTO userDTO, final HttpServletRequest request){
        try {
            // Register the user
            User user = userService.registerAccount(userDTO);

            // Publish the registration event
            applicationEventPublisher.publishEvent(new RegistrationEvent(user, applicationUrl(request)));

            // Return success response
            return new ResponseEntity<>("Success! Please, check your email to complete your registration.", HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            // Return error response
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token){
        VerificationToken tokens = verificationTokenRepository.findByToken(token);
        if(tokens.getUser().isEnabled()){
            return "This account has already been verified, please login";
        }
        String verificationResult = userService.validateToken(token);
        if(verificationResult.equalsIgnoreCase("Valid")){
            return "Email verified successfully. Now you can login to your account";
        }
        return "Invalid verification token";
    }

    public String applicationUrl(HttpServletRequest request) {
        return "http://"+request.getServerName()+":"+request.getServerPort()+":"+request.getContextPath();
    }
}
