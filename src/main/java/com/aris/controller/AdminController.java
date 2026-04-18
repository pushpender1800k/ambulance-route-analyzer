package com.aris.controller;

import com.aris.model.Ambulance;
import com.aris.model.Role;
import com.aris.model.User;
import com.aris.repository.AmbulanceRepository;
import com.aris.repository.UserRepository;
import com.aris.websocket.EventBroadcaster;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('COMMAND', 'COORDINATOR', 'SUPERVISOR')")
public class AdminController {

    private final UserRepository userRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventBroadcaster eventBroadcaster;

    public AdminController(UserRepository userRepository, AmbulanceRepository ambulanceRepository,
                          PasswordEncoder passwordEncoder, EventBroadcaster eventBroadcaster) {
        this.userRepository = userRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventBroadcaster = eventBroadcaster;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> userList = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("role", u.getRole().name());
            map.put("fullName", u.getFullName());
            map.put("phone", u.getPhone());
            map.put("email", u.getEmail());
            map.put("isActive", u.getIsActive());
            map.put("lastLogin", u.getLastLogin());
            if (u.getRole() == Role.DRIVER && u.getAssignedAmbulanceId() != null) {
                map.put("assignedAmbulanceId", u.getAssignedAmbulanceId());
                ambulanceRepository.findById(u.getAssignedAmbulanceId()).ifPresent(amb -> 
                    map.put("assignedAmbulanceCode", amb.getUnitCode()));
            }
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(userList);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String role = (String) request.get("role");
        String fullName = (String) request.get("fullName");
        String phone = (String) request.get("phone");
        String email = (String) request.get("email");

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        Role userRole;
        try {
            userRole = Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }

        User user = new User(username, passwordEncoder.encode(password), userRole);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user = userRepository.save(user);

        eventBroadcaster.broadcast("ADMIN", 
            String.format("New user created: %s with role %s", username, role), "ADMIN");

        return ResponseEntity.ok(Map.of(
            "message", "User created successfully",
            "userId", user.getId()
        ));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.containsKey("fullName")) {
            user.setFullName((String) request.get("fullName"));
        }
        if (request.containsKey("phone")) {
            user.setPhone((String) request.get("phone"));
        }
        if (request.containsKey("email")) {
            user.setEmail((String) request.get("email"));
        }
        if (request.containsKey("role")) {
            try {
                user.setRole(Role.valueOf((String) request.get("role")));
            } catch (IllegalArgumentException ignored) {}
        }
        if (request.containsKey("isActive")) {
            user.setIsActive((Boolean) request.get("isActive"));
        }
        if (request.containsKey("password") && request.get("password") != null) {
            String newPassword = (String) request.get("password");
            if (!newPassword.isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
            }
        }

        userRepository.save(user);

        eventBroadcaster.broadcast("ADMIN", 
            String.format("User updated: %s", user.getUsername()), "ADMIN");

        return ResponseEntity.ok(Map.of("message", "User updated successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (user.getRole() == Role.COMMAND) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete COMMAND user"));
        }

        userRepository.delete(user);

        eventBroadcaster.broadcast("ADMIN", 
            String.format("User deleted: %s", user.getUsername()), "ADMIN");

        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/ambulances")
    public ResponseEntity<?> getAllAmbulances() {
        List<Ambulance> ambulances = ambulanceRepository.findAll();
        List<Map<String, Object>> ambList = ambulances.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("unitCode", a.getUnitCode());
            map.put("lat", a.getLat());
            map.put("lng", a.getLng());
            map.put("status", a.getStatus().name());
            map.put("vehicleNumber", a.getVehicleNumber());
            map.put("vehicleModel", a.getVehicleModel());
            map.put("baseLocation", a.getBaseLocation());
            map.put("equipmentLevel", a.getEquipmentLevel());
            map.put("assignedDriverId", a.getAssignedDriverId());
            map.put("assignedDriverName", a.getAssignedDriverName());
            map.put("assignedDispatcherId", a.getAssignedDispatcherId());
            map.put("assignedDispatcherName", a.getAssignedDispatcherName());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ambList);
    }

    @PutMapping("/ambulances/{id}/assign-driver")
    public ResponseEntity<?> assignDriver(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Ambulance ambulance = ambulanceRepository.findById(id).orElse(null);
        if (ambulance == null) {
            return ResponseEntity.notFound().build();
        }

        Long driverId = request.get("driverId") != null ? Long.parseLong(request.get("driverId").toString()) : null;
        
        if (driverId != null) {
            User driver = userRepository.findById(driverId).orElse(null);
            if (driver == null || driver.getRole() != Role.DRIVER) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid driver"));
            }

            if (driver.getAssignedAmbulanceId() != null && !driver.getAssignedAmbulanceId().equals(id)) {
                Ambulance oldAmb = ambulanceRepository.findById(driver.getAssignedAmbulanceId()).orElse(null);
                if (oldAmb != null) {
                    oldAmb.setAssignedDriverId(null);
                    oldAmb.setAssignedDriverName(null);
                    ambulanceRepository.save(oldAmb);
                }
            }

            ambulance.setAssignedDriverId(driverId);
            ambulance.setAssignedDriverName(driver.getFullName());
            driver.setAssignedAmbulanceId(id);
            userRepository.save(driver);
        } else {
            if (ambulance.getAssignedDriverId() != null) {
                User oldDriver = userRepository.findById(ambulance.getAssignedDriverId()).orElse(null);
                if (oldDriver != null) {
                    oldDriver.setAssignedAmbulanceId(null);
                    userRepository.save(oldDriver);
                }
            }
            ambulance.setAssignedDriverId(null);
            ambulance.setAssignedDriverName(null);
        }

        ambulanceRepository.save(ambulance);

        return ResponseEntity.ok(Map.of("message", "Driver assignment updated"));
    }

    @PutMapping("/ambulances/{id}/assign-dispatcher")
    public ResponseEntity<?> assignDispatcher(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Ambulance ambulance = ambulanceRepository.findById(id).orElse(null);
        if (ambulance == null) {
            return ResponseEntity.notFound().build();
        }

        Long dispatcherId = request.get("dispatcherId") != null ? Long.parseLong(request.get("dispatcherId").toString()) : null;
        String dispatcherName = (String) request.get("dispatcherName");
        
        ambulance.setAssignedDispatcherId(dispatcherId);
        ambulance.setAssignedDispatcherName(dispatcherName);
        ambulanceRepository.save(ambulance);

        return ResponseEntity.ok(Map.of("message", "Dispatcher assignment updated"));
    }

    @GetMapping("/drivers")
    public ResponseEntity<?> getAllDrivers() {
        List<Map<String, Object>> drivers = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.DRIVER)
            .map(d -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", d.getId());
                map.put("username", d.getUsername());
                map.put("fullName", d.getFullName());
                map.put("phone", d.getPhone());
                map.put("assignedAmbulanceId", d.getAssignedAmbulanceId());
                if (d.getAssignedAmbulanceId() != null) {
                    ambulanceRepository.findById(d.getAssignedAmbulanceId()).ifPresent(amb -> 
                        map.put("assignedAmbulanceCode", amb.getUnitCode()));
                }
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/dispatchers")
    public ResponseEntity<?> getAllDispatchers() {
        List<Map<String, Object>> dispatchers = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.DISPATCHER)
            .map(d -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", d.getId());
                map.put("username", d.getUsername());
                map.put("fullName", d.getFullName());
                map.put("phone", d.getPhone());
                map.put("isActive", d.getIsActive());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(dispatchers);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        long totalUsers = userRepository.count();
        long totalDrivers = userRepository.findAll().stream().filter(u -> u.getRole() == Role.DRIVER).count();
        long totalDispatchers = userRepository.findAll().stream().filter(u -> u.getRole() == Role.DISPATCHER).count();
        long totalPatients = userRepository.findAll().stream().filter(u -> u.getRole() == Role.PATIENT).count();
        long totalAmbulances = ambulanceRepository.count();
        long standbyAmbulances = ambulanceRepository.findByStatus(com.aris.model.AmbulanceStatus.STANDBY).size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalDrivers", totalDrivers);
        stats.put("totalDispatchers", totalDispatchers);
        stats.put("totalPatients", totalPatients);
        stats.put("totalAmbulances", totalAmbulances);
        stats.put("standbyAmbulances", standbyAmbulances);
        stats.put("deployedAmbulances", totalAmbulances - standbyAmbulances);

        return ResponseEntity.ok(stats);
    }
}
