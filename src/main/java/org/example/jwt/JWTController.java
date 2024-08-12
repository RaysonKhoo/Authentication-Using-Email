package org.example.jwt;

import lombok.RequiredArgsConstructor;
import org.example.Entity.User;
import org.example.dto.JwtDTO;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/authentication")
public class JWTController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserService userService;

//    @PostMapping
//    public String getTokenForAuthenticationUser(@RequestBody JWTAuthenticationRequest authRequest) {
//        Authentication authentication = authenticationManager
//                .authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
//        if (authentication.isAuthenticated()) {
//            return jwtService.getGenerateToken(authRequest.getEmail());
//        } else {
//            throw new UsernameNotFoundException("Invalid User Credentials");
//        }
//    }
@PostMapping
public ResponseEntity<JwtDTO> getTokenForAuthenticationUser(@RequestBody JWTAuthenticationRequest authRequest) {
    Authentication authentication = authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
    if (authentication.isAuthenticated()) {
        String token = jwtService.getGenerateToken(authRequest.getEmail());
        User user = userService.getUserByEmail(authRequest.getEmail()); // Fetch user details

        JwtDTO response = new JwtDTO();
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setRole(user.getRole());
        response.setEnabled(user.isEnabled());
        response.setAccessToken(token);
        response.setCreatedBy(user.getCreatedBy());
        response.setCreatedOn(user.getCreatedOn());
        response.setUpdatedBy(user.getUpdatedBy());
        response.setUpdatedOn(user.getUpdatedOn());

        return new ResponseEntity<>(response, HttpStatus.OK);
    } else {
        throw new UsernameNotFoundException("Invalid User Credentials");
    }
}

}
