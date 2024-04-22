package com.telegramBot.service;

import com.telegramBot.model.News;
import com.telegramBot.model.enums.Source;
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

    public List<News> parseNewNews(Source source) throws IOException {
        if(source.equals(Source.RIA)){
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

                Elements tagsElements = newsDocument.select(".article__tags .article__tags-item");

                List<String> tags = new ArrayList<>();
                for (Element tagElement : tagsElements) {
                    String tagName = tagElement.text().trim();
                    tags.add(tagName);
                }

                Element imageElement = newsDocument.selectFirst("img");
                String imageUrl = imageElement != null ? imageElement.attr("src") : "";
                news.setImageUrl(imageUrl);

                news.setList(newsService.listWithTagsFromStringList(tags));
                news.setSource(Source.RIA);
                listNews.add(news);
            }
            return listNews;
        }else{
            Document document = Jsoup.connect("https://www.rbc.ru/economics/?utm_source=topline").get();
            Elements listItems = document.getElementsByClass("item__link");
            List<News> listNews = new ArrayList<>();
            for (Element item : listItems) {
                News news = new News();
                String newsLink = item.attr("href");
                String titleElement = item.text();

                news.setLink(newsLink);
                news.setTitle(titleElement);

                Document newsDocument = Jsoup.connect(newsLink).get();
                StringBuilder  allText = new StringBuilder();

                for (Element block :  newsDocument.getElementsByClass("article__text article__text_free")) {
                    allText.append(block.select("p").text());
                }

                news.setMainText(allText.toString());
                Element imageElement = newsDocument.selectFirst(".article__main-image img");
                String imageUrl = imageElement != null ? imageElement.attr("src") : "";


                Elements tagsElements = newsDocument.select(".article__tags__container .article__tags__item");
                List<String> tags = new ArrayList<>();
                for (Element tagElement : tagsElements) {
                    String tagName = tagElement.text();
                    tags.add(tagName);
                }
                news.setImageUrl(imageUrl);
                news.setMainText(allText.toString());
                news.setSource(Source.RBC);
                news.setList(newsService.listWithTagsFromStringList(tags));
                listNews.add(news);
            }
            return listNews;
        }
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
