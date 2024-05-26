package com.telegramBot.repository;

import com.telegramBot.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NewsRepository extends JpaRepository<News,Long> {
    News findByLink(String link);

}

