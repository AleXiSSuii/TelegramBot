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

    public void parseNewNews(Source source) throws IOException {
        if(source.equals(Source.RIA)){
            Document document = Jsoup.connect("https://ria.ru/tag_finansy/").get();
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
                boolean skip = true;
                for (Element block : newsDocument.getElementsByClass("article__block")) {
                    if (block.attr("data-type").equals("text")) {
                        if(skip){
                            skip = false;
                            continue;
                        }
                        Element textElement = block.selectFirst(".article__text");
                        String blockText = textElement.text();
                        if (blockText.contains("Такого Telegram-канала, как у нас, нет ни у кого. Он для тех, кто хочет делать выводы сам.")) {
                            continue;
                        }
                        allText.append(blockText).append("\n");
                    }
                }
                if(allText.length() < 45){
                    continue;
                }
                news.setMainText(allText.toString());

                Elements tagsElements = newsDocument.select(".article__tags .article__tags-item");

                List<String> tags = new ArrayList<>();
                for (Element tagElement : tagsElements) {
                    String tagName = tagElement.text().trim();
                    if (tagName.equals("Финансы")){
                        continue;
                    }
                    tags.add(tagName);
                }

                Element imageElement = newsDocument.selectFirst("img");
                String imageUrl = imageElement != null ? imageElement.attr("src") : "";
                news.setImageUrl(imageUrl);

                news.setSource(Source.RIA);
                listNews.add(news);
                newsService.saveNewsWithTags(news,tags);
            }
        }else{
            Document document = Jsoup.connect("https://www.rbc.ru/finances/").get();
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
                if(allText.length() < 45){
                    continue;
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
                listNews.add(news);
                newsService.saveNewsWithTags(news,tags);
            }
        }
    }
}
