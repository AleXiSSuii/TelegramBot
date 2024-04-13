package com.telegramBot.parsers.news;

import com.telegramBot.parsers.news.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag,Integer> {
    Tag findTagByTitle(String title);
}
