package com.telegramBot.repository;

import com.telegramBot.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag,Integer> {
    Tag findTagByTitle(String title);
}
