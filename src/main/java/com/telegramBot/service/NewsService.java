package com.telegramBot.service;

import com.telegramBot.model.News;
import com.telegramBot.model.Tag;
import com.telegramBot.repository.NewsRepository;
import com.telegramBot.repository.TagRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private TagRepository tagRepository;

    public List<Tag> listWithTagsFromString(String tagsLine){
        List<Tag> allTags = new ArrayList<>();
        String[] tags = tagsLine.split(", ");
        if(!tagsLine.isEmpty()){
            for (String s : tags) {
                Tag existingTag = tagRepository.findTagByTitle(s);
                System.out.println(existingTag);
                if(existingTag != null){
                    allTags.add(existingTag);
                }else{
                    Tag tag = new Tag();
                    tag.setTitle(s);
                    allTags.add(tag);
                    tagRepository.save(tag);
                }
            }
        }
        return allTags;
    }

    public void saveNewsInDataBase(List<News> listNews){
        for (News news : listNews) {
            System.out.println(news.getLink());
            if (newsRepository.findByLink(news.getLink()) == null) {
                news.setDateTime(LocalDateTime.now());
                news.setCheckSend(false);
                newsRepository.save(news);
            }
        }
    }

    public List<News> getAllNews(){
        return newsRepository.findAll();
    }

    public List<News> getAllNewsSortByDate(){
        return newsRepository.findAll(Sort.by(Sort.Direction.DESC, "dateTime"));
    }
    public void checkerIsTrue(News news){
        news.setCheckSend(true);
        newsRepository.save(news);
    }

}
