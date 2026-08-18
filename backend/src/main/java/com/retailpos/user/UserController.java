package com.retailpos.user;

import com.retailpos.domain.User;
import com.retailpos.domain.UserRepository;
import com.retailpos.domain.Role;
import com.retailpos.domain.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findByIsDeletedFalse();
        users.forEach(u -> {
            roleRepository.findById(u.getRoleId()).ifPresent(r -> u.setRoleName(r.getName()));
        });
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user.getStatus() == null) user.setStatus("ACTIVE");
        if (user.getRoleId() == null) user.setRoleId(2L);
        if (user.getIsDeleted() == null) user.setIsDeleted(false);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User details) {
        return userRepository.findById(id).map(existing -> {
            if (details.getFullName() != null) existing.setFullName(details.getFullName());
            if (details.getEmail() != null) existing.setEmail(details.getEmail());
            if (details.getRoleId() != null) existing.setRoleId(details.getRoleId());
            if (details.getStatus() != null) existing.setStatus(details.getStatus());
            return ResponseEntity.ok(userRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setIsDeleted(true);
            userRepository.save(u);
        });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }
}
