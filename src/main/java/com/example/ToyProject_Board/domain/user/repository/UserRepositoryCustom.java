package com.example.ToyProject_Board.domain.user.repository;

import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.dto.request.UserSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> searchUsers(UserSearchRequest request, Pageable pageable);
}
