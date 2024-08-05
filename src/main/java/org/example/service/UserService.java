package org.example.service;

import org.example.Entity.VerificationToken;
import org.example.dto.UserDTO;
import org.example.repository.UserRepository;
import org.example.Entity.User;
import org.example.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

@Service
public class UserService {

    @Autowired
    public final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public User registerAccount (UserDTO userDTO){
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        Optional<User> userExits =userRepository.findUserByEmail(userDTO.getEmail());
        if(userExits.isPresent()){
            throw new IllegalStateException("Email " + userDTO.getEmail() + " already exists");
        }
        if(userDTO.getEmail()==null)
        {
            throw new IllegalStateException("Email cannot be null");
        }
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setDateOfBirth(LocalDate.of(userDTO.getYear(), userDTO.getMonth(), userDTO.getDay()));
        user.setRole(userDTO.getRole());
        user.setCreatedBy("admin");
        user.setUpdatedBy("admin");

        return  userRepository.save(user);
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public User getUserByEmail(String email){
        Optional<User> user = userRepository.findUserByEmail(email);
        if(user.isPresent()){
            return user.get();
        }else {
            throw new IllegalStateException("User email not found");
        }
    }

    public void saveUserVerificationToken(User user, String token) {

        var verificationToken = new VerificationToken(token,user);

        verificationTokenRepository.save(verificationToken);
    }

    public String validateToken(String tokens) {
        VerificationToken token = verificationTokenRepository.findByToken(tokens);
        if(token == null){
            return "Invalid Verification token";
        }
        User user = token .getUser();
        Calendar calendar = Calendar.getInstance();
        if((token.getTokenExpirationTime().getTime() -calendar.getTime().getTime()) <=0)
        {
            verificationTokenRepository.delete(token);
            return "Token already expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "Valid";
    }
}
