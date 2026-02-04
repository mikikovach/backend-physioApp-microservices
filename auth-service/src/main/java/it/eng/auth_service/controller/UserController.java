package it.eng.auth_service.controller;

import it.eng.auth_service.dto.UpdateUserDTO;
import it.eng.auth_service.dto.UserResponseDTO;
import it.eng.auth_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUser(@RequestHeader("X-User-Email") String email) {

//        String userName = request.getAttribute("userName").toString();
        Long userId = userService.getUserIdByUsername(email);

        UserResponseDTO userResponseDTO = userService.getUser(userId);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
    }

    @PutMapping("/edit")
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody UpdateUserDTO updateUserDTO,
                                                      @RequestHeader("X-User-Email") String email

    ) {
        Long userId = userService.getUserIdByUsername(email);
        UserResponseDTO userResponseDTO = userService.editUser(userId, updateUserDTO);

        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/test-headers")
    public ResponseEntity<Map<String, String>> testHeaders(@RequestHeader Map<String, String> headers) {
        return ResponseEntity.ok(headers);
    }

}

