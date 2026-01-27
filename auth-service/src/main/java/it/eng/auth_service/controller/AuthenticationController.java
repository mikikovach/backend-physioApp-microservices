package it.eng.auth_service.controller;


import it.eng.auth_service.dto.LoginUserDto;
import it.eng.auth_service.dto.UserDTO;
import it.eng.auth_service.entity.User;
import it.eng.auth_service.service.AuthenticationService;
import it.eng.auth_service.service.JwtService;
import it.eng.auth_service.util.LoginResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;


     @PostMapping("/signup")
     public ResponseEntity<LoginResponse> signup(@RequestBody UserDTO registerUserDto) {
         User registeredUser  = authenticationService.signup(registerUserDto);
         String jwtToken = jwtService.generateToken(registeredUser);
         LoginResponse loginResponse = authenticationService.buildLoginresponse(registeredUser, jwtToken, jwtService.getExpirationTime());
         return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse );
     }


     @PostMapping("/login")
     public ResponseEntity<LoginResponse> login(@RequestBody LoginUserDto loginUserDto) {

         System.out.println(" Auth kontroler : " + loginUserDto.email());
         User authenticatedUser = authenticationService.authenticate(loginUserDto);
         String jwtToken = jwtService.generateToken(authenticatedUser);

         LoginResponse loginResponse = authenticationService.buildLoginresponse(authenticatedUser, jwtToken, jwtService.getExpirationTime());

         return ResponseEntity.ok(loginResponse);
     }
}
