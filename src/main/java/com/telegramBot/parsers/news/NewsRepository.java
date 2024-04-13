package com.telegramBot.parsers.news;

import com.telegramBot.parsers.news.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News,Long> {
    News findByLink(String link);
}

