package com.anas.chatservice.user;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Cacheable(value = "users", key = "#connectedUser.name")

    public List<UserResponse> finAllUsersExceptSelf(Authentication connectedUser) {
        return userRepository.findAllUsersExceptSelf(connectedUser.getName())
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }


    @Cacheable(value = "users:id", key = "#id")
    public Optional<UserResponse> findById(String id) {
        return userRepository.findById(id)
                .map(userMapper::toUserResponse);
    }


}
