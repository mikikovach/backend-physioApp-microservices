package it.eng.auth_service.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@AllArgsConstructor
@Slf4j
public class AdminUserController {

    @GetMapping
    public String getAllUsers() {
        log.info("AdminUserController: getAllUsers called");
        return "This endpoint will return all users (admin only)";
    }
}
