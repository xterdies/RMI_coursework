package com.platform.api.controller;

import com.platform.api.dto.CommonDtos;
import com.platform.api.dto.UserDto;
import com.platform.service.AuditService;
import com.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only user and system management")
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public CommonDtos.PagedResponse<UserDto> listUsers(@PageableDefault(size = 20) Pageable pageable) {
        return CommonDtos.PagedResponse.from(userService.findAll(pageable));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public UserDto getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role")
    public UserDto updateRole(@PathVariable Long id, @RequestParam String role) {
        return userService.updateRole(id, role);
    }

    @PatchMapping("/users/{id}/toggle")
    @Operation(summary = "Enable/disable user account")
    public UserDto toggleUser(@PathVariable Long id) {
        return userService.toggleEnabled(id);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }

    @GetMapping("/logs")
    @Operation(summary = "Get system audit logs")
    public CommonDtos.PagedResponse<Object> getLogs(@PageableDefault(size = 50) Pageable pageable) {
        return CommonDtos.PagedResponse.from(auditService.findAll(pageable).map(log ->
                (Object) new java.util.LinkedHashMap<String, Object>() {{
                    put("id", log.getId());
                    put("action", log.getAction());
                    put("entityType", log.getEntityType());
                    put("entityId", log.getEntityId());
                    put("details", log.getDetails());
                    put("status", log.getStatus());
                    put("createdAt", log.getCreatedAt());
                }}));
    }
}
