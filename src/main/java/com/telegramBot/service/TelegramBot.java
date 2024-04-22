package com.telegramBot.service;

import com.telegramBot.configuration.BotConfiguration;
import com.telegramBot.model.Currency;
import com.telegramBot.model.News;
import com.telegramBot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private Boolean checkKeyboard = null;

    @Autowired
    private BotConfiguration botConfiguration;

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParseService parseService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private InlineKeyboardService inlineKeyboardService;

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
                case "/currency":
                    currencyMenu(chatId);
                    break;
                case "Популярные валюты":
                    topValute(chatId, inlineKeyboardService.getTopValuteKeyboard());
                    checkKeyboard = true;
                    break;
                case "Все валюты":
                    allValute(chatId,inlineKeyboardService.getAllValuteKeyboard());
                    checkKeyboard = false;
                    break;
                default:
                    sendMessage(chatId, "Sorry,command is not available");
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            System.out.println(messageId);
            Currency currency = currencyService.findByCodeValute(data);
            String text = "Курс " + currency.getName() + "=" + currency.getValue();
            if(checkKeyboard.equals(true)){
                updateMessageText(chatId, messageId, text,inlineKeyboardService.getTopValuteKeyboard());
            }else {
                updateMessageText(chatId, messageId, text,inlineKeyboardService.getAllValuteKeyboard());
            }
        }
    }

    private void topValute(long chatId, InlineKeyboardMarkup topValuteKeyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите валюту");

        message.setReplyMarkup(topValuteKeyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void allValute(long chatId, InlineKeyboardMarkup allValuteKeyboard){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите валюту");
        message.setReplyMarkup(allValuteKeyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }


    //@Scheduled(cron = "0 * * * * *")
    private void sendNews() {
        List<User> usersList = userService.getAllUsers();
        News news = parseService.postNewNews();
        File image = imageService.saveImage(news.getImageUrl());
        if (news.getTitle() != null && news.getMainText() != null) {
            for (User user : usersList) {
                sendPhotoMessage(user, news.getTitle() + "\n" + news.getMainText(), image);

            }
            newsService.checkerIsTrue(news);
        } else {
            log.info("Нет подходящих новостей для публикации");
        }
    }

    private void sendPhotoMessage(User user, String text, File image) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(user.getChatId()));
        if (image != null) {
            sendPhoto.setPhoto(new InputFile(image));
            try {
                execute(sendPhoto);
            } catch (TelegramApiRequestException error) {
                if (error.getErrorCode() == 403) {
                    log.info("Пользователь " + user.getChatId() + " заблокировал бота");
                    return;
                }
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки картинки", e);
            }
        } else {
            log.info("Нет картинки для добавления или произошла ошибка");
        }
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(user.getChatId()));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiRequestException error) {
            if (error.getErrorCode() == 403) {
                log.info("Пользователь " + user.getChatId() + " заблокировал бота");
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    private void currencyMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Курс валют");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("Популярные валюты");
        row.add("Все валюты");

        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("Вернуться назад");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);


        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("error");
        }
    }

    private void updateMessageText(long chatId, int messageId, String newText, InlineKeyboardMarkup valuteKeyboard) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.setText(newText);
        editMessageText.setReplyMarkup(valuteKeyboard);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("message can't be edited")) {
                sendMessage(chatId, "Unfortunately, this message cannot be edited anymore.");
            } else {
                log.error(e + " ");
            }
        }
    }
}
