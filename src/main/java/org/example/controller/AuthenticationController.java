package org.example.controller;

import org.example.dto.UserDTO;
import org.example.service.UserService;
import org.example.Entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/api/user")
public class AuthenticationController {

    private final UserService userService;
    public AuthenticationController(UserService userService){
        this.userService =userService;
    }

//    @PostMapping(path ="/add")
//    public ResponseEntity<Object> registerUser(@RequestBody UserDTO userDTO) {
//        try {
//            User user = userService.registerAccount(userDTO);
//            return new ResponseEntity<>(user, HttpStatus.CREATED);
//        } catch (IllegalStateException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }

    @GetMapping(path = "/list")
    public ResponseEntity<List<User>> getALlUserDetails(){
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
    }

    @GetMapping(path = "/list/{email}")
    public ResponseEntity<Object> getUserByEmail(@PathVariable String email) {
        try {
            return new ResponseEntity<>(userService.getUserByEmail(email), HttpStatus.OK);
        }catch (IllegalStateException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
