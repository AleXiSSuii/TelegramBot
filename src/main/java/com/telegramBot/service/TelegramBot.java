package com.telegramBot.service;

import com.telegramBot.configuration.BotConfiguration;
import com.telegramBot.model.News;
import com.telegramBot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private BotConfiguration botConfiguration;

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParseService parseService;

    static final String HELP_TEXT = "Это бот агрегатор новостей в экономеческой сфере";

    public TelegramBot(BotConfiguration botConfiguration) {
        this.botConfiguration = botConfiguration;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/news", "get news"));
        listOfCommands.add(new BotCommand("/currency", "Получить свежий курс валют"));
        listOfCommands.add(new BotCommand("/tags", "Новости по тэгам"));
        listOfCommands.add(new BotCommand("/help", "Информация о боте"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot`s command list: " + e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return botConfiguration.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfiguration.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    userService.registerUser(update.getMessage());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Sorry,command is not available");
            }
        }

    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = "Привет" + firstName + ",nice to meet you";
        log.info("Replied to user " + firstName);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("error");
        }
    }


    @Scheduled(cron = "0 * * * * *")
    private void sendNews() throws MalformedURLException {
        List<User> usersList = userService.getAllUsers();
        News news = parseService.postNewNews();
        if(news.getTitle() !=null && news.getMainText() !=null){
            for (User user : usersList) {
                sendMessage(user.getChatId(), news.getTitle() + "\n" + news.getMainText());
                newsService.checkerIsTrue(news);
            }
        }else {
            log.info("Нет подходящих новостей для публикации");
        }
    }
}
