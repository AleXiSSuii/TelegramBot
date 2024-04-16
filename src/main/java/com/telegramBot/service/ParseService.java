package com.telegramBot.service;

import com.telegramBot.model.News;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ParseService {

    @Autowired
    private NewsService newsService;

    public List<News> parseNewNews() throws IOException {
        Document document = Jsoup.connect("https://ria.ru/economy/").get();
        Elements listItems = document.getElementsByClass("list-item__title color-font-hover-only");
        List<News> listNews = new ArrayList<>();
        for (Element item : listItems.reversed()) {
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
                    allText.append(blockText).append("\n");
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
            Element imageElement = newsDocument.selectFirst("img");
            String imageUrl = imageElement != null ? imageElement.attr("src") : "";
            news.setImageUrl(imageUrl);

            news.setList(newsService.listWithTagsFromString(pageTags));
            listNews.add(news);
        }
        return listNews;
    }
    public News postNewNews(){
        List<News> listNews = newsService.getAllNewsSortByDate();
        News postNews = new News();
        for(News news:listNews){
            if(news.getCheckSend().equals(true)){
                System.out.println(postNews.getTitle());
                return postNews;
            }
            postNews = news;
        }
        return postNews;
    }
}
