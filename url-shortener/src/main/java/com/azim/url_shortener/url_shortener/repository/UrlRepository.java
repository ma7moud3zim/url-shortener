package com.azim.url_shortener.url_shortener.repository;

import com.azim.url_shortener.url_shortener.entity.Url;
import com.azim.url_shortener.url_shortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    Optional<Url> findByCustomAlias(String customAlias);
    List<Url> findByUser(User user);
    boolean existsByShortCode(String shortCode);
    boolean existsByCustomAlias(String customAlias);
}