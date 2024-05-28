package com.telegramBot.service;

import com.telegramBot.model.News;
import com.telegramBot.model.User;
import com.telegramBot.model.enums.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;


@Slf4j
@Service
public class ScheduledService {
    @Autowired
    private ParseService parseService;
    @Autowired
    private CurrencyService currencyService;
    @Autowired
    private UserService userService;
    @Autowired
    private NewsService newsService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private TelegramBot telegramBot;

    @Scheduled(cron = "30 31 * * * *")
    private void getNewNews() {
        try {
            parseService.parseNewNews();
            log.info("Все новые новости с \"Риа Новости\" и \"РБК Новости\" успешно загружены");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 30 * * * *")
    private void updateCurrency() {
        currencyService.updateCurrency(currencyService.parseCurrency());
        log.info("Актуальные курсы валют обновлены");
    }

    @Scheduled(cron = "0 0 * * * *")
    private void sendNews() {
        List<User> usersList = userService.getAllUsers();
        News news = newsService.postNewNewsToAllUsers();
        File image = imageService.saveImage(news.getImageUrl());
        if (news.getTitle() != null && news.getMainText() != null) {
            for (User user : usersList) {
                telegramBot.sendPhotoMessage(user.getChatId(), news.getTitle()
                        + "\n" + "\n" + news.getMainText()
                        + "\n" + "\n" + "Источник: " + Source.getStringSource(news.getSource()), image);
            }
            newsService.checkerIsTrue(news);
            log.info("Новые новости отправлены");
        } else {
            log.info("Нет подходящих новостей для публикации");
        }
    }
}
