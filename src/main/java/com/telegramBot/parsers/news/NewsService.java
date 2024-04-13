package com.telegramBot.parsers.news;

import com.telegramBot.parsers.news.model.News;
import com.telegramBot.parsers.news.model.Tag;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<News> parseNewNews() throws IOException {
        Document document = Jsoup.connect("https://ria.ru/economy/").get();
        Elements listItems = document.getElementsByClass("list-item__title color-font-hover-only");
        List<News> listNews = new ArrayList<>();
        for (Element item : listItems) {
            News news = new News();
            Element titleElement = item.selectFirst("a");
            String title = item.text();
            String newsLink = "";
            if(titleElement != null){
                newsLink = titleElement.attr("href");
                news.setLink(newsLink);
            }
            news.setTitle(title);

            Document newsDocument = Jsoup.connect(newsLink).get();
            StringBuilder  allText = new StringBuilder();

            for (Element block : newsDocument.getElementsByClass("article__block")) {
                if (block.attr("data-type").equals("text")) {
                    Element textElement = block.selectFirst(".article__text");
                    String blockText = textElement.text();
                    allText.append(blockText + "\n");
                }
            }
            news.setMainText(allText.toString());
            String scriptData = newsDocument.selectFirst("script[type='text/javascript']").data();
            String pageTags = "";
            int startIndex = scriptData.indexOf("'page_tags' : '") + 15;
            int endIndex = scriptData.indexOf("'", startIndex);
            if (startIndex > -1 && endIndex > startIndex) {
                pageTags = scriptData.substring(startIndex, endIndex);
            }
            news.setList(listWithTagsFromString(pageTags));
            listNews.add(news);
        }
        return listNews;
    }

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
        List<News> savedNews = newsRepository.findAll();
        listNews.reversed();
        for (News news : listNews) {
            System.out.println(news.getLink());
            if (newsRepository.findByLink(news.getLink()) == null) {
                news.setDateTime(LocalDateTime.now());
                newsRepository.save(news);
            } else {
                break;
            }
        }
    }
}
