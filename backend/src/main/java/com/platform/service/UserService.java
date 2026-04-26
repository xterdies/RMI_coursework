package com.platform.service;

import com.platform.api.dto.UserDto;
import com.platform.api.mapper.EntityMapper;
import com.platform.domain.entity.Role;
import com.platform.domain.entity.User;
import com.platform.domain.repository.RoleRepository;
import com.platform.domain.repository.UserRepository;
import com.platform.infrastructure.query.FilterCriterion;
import com.platform.infrastructure.query.Specifications;
import com.platform.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper::toUserDto);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable, List<FilterCriterion> criteria) {
        Specification<User> spec = Specifications.fromCriteria(criteria, List.of("email", "fullName", "enabled", "role.name"));
        return userRepository.findAll(spec, pageable).map(mapper::toUserDto);
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return mapper.toUserDto(getOrThrow(id));
    }

    @Transactional
    public UserDto updateRole(Long userId, String roleName) {
        User user = getOrThrow(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        user.setRole(role);
        return mapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto toggleEnabled(Long userId) {
        User user = getOrThrow(userId);
        user.setEnabled(!user.isEnabled());
        return mapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
