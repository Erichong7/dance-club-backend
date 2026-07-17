package com.example.ToyProject_Board.domain.post.repository;

import com.example.ToyProject_Board.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
