package com.azim.url_shortener.url_shortener.repository;

import com.azim.url_shortener.url_shortener.entity.UrlBlocklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UrlBlocklistRepository extends JpaRepository<UrlBlocklist, Long> {
    boolean existsByUrl(String url);
    Optional<UrlBlocklist> findByUrl(String url);
}