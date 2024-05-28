package com.telegramBot.service;

import com.telegramBot.model.News;
import com.telegramBot.model.Tag;
import com.telegramBot.model.User;
import com.telegramBot.repository.NewsRepository;
import com.telegramBot.repository.TagRepository;
import com.telegramBot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void saveNewsWithTags(News news, List<String> tagsLine) {
        HashSet<Tag> allTags = new HashSet<>();
        if (!tagsLine.isEmpty() && newsRepository.findByLink(news.getLink()) == null) {
            for (String s : tagsLine) {
                Tag tag = tagRepository.findTagByTitle(s);
                if (tag == null) {
                    tag = new Tag();
                    tag.setTitle(s);
                    tag.setNewsCount(1);
                } else {
                    tag.setNewsCount(tag.getNewsCount() + 1);
                }
                tag.getNews().add(news);
                allTags.add(tag);
            }
            tagRepository.saveAll(allTags);
            news.setTags(allTags);
            news.setDateTime(LocalDateTime.now());
            news.setCheckSend(false);
            newsRepository.save(news);
        }
    }

    public List<News> getAllNewsSortByDate() {
        return newsRepository.findAll(Sort.by(Sort.Direction.DESC, "dateTime"));
    }

    public void checkerIsTrue(News news) {
        news.setCheckSend(true);
        newsRepository.save(news);
    }
    @Transactional
    public News getNewsByTagForUser(long chatId, String tag) {
        List<News> news = newsRepository.findAll();
        Tag targetTag = tagRepository.findByTitleIgnoreCase(tag);

        User user = userRepository.findById(chatId).orElseThrow(() -> new NoSuchElementException("Пользователь с id(" + chatId + ") не найден"));
        Set<News> seenNews = user.getSeenNews();
        if(targetTag != null){
            for (int i = news.size() - 1; i >= 0; i--) {
                Set<Tag> tags = news.get(i).getTags();
                for (Tag t : tags) {
                    if (t.getTitle().equals(targetTag.getTitle()) && !seenNews.contains(news.get(i))) {
                        seenNews.add(news.get(i));
                        user.setSeenNews(seenNews);
                        userRepository.save(user);
                        return news.get(i);
                    }
                }
            }
        }
        return null;
    }

    public News postNewNewsToAllUsers() {
        List<News> listNews = getAllNewsSortByDate();
        News postNews = new News();
        for (News news : listNews) {
            if (news.getCheckSend().equals(true)) {
                System.out.println(postNews.getTitle());
                return postNews;
            }
            postNews = news;
        }
        return postNews;
    }

    public Tag findTagIgnoreTestcase(String tag){
        return tagRepository.findByTitleIgnoreCase(tag);
    }
}
