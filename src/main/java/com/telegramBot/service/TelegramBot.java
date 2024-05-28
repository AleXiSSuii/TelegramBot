package com.telegramBot.service;

import com.telegramBot.configuration.BotConfiguration;
import com.telegramBot.model.Currency;
import com.telegramBot.model.News;
import com.telegramBot.model.enums.Source;
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

import static com.telegramBot.utils.Constants.HELP_TEXT;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private Boolean checkKeyboard = null;
    private boolean isTagSearchActive = false;
    private long tagSearchChatId = -1;

    @Autowired
    private BotConfiguration botConfiguration;

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private InlineKeyboardService inlineKeyboardService;


    public TelegramBot(BotConfiguration botConfiguration) {
        this.botConfiguration = botConfiguration;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начало работы чат-бота агрегатора"));
        listOfCommands.add(new BotCommand("/news", "Новость по тэгу"));
        listOfCommands.add(new BotCommand("/currency", "Получить свежий курс валют"));
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

            if (isTagSearchActive && chatId == tagSearchChatId) {
                if (newsService.findTagIgnoreTestcase(messageText) != null) {
                    News news = newsService.getNewsByTagForUser(chatId, messageText);
                    if (news != null) {
                        File image = imageService.saveImage(news.getImageUrl());
                        sendPhotoMessage(chatId, news.getTitle()
                                + "\n" + "\n" + news.getMainText()
                                + "\n" + "\n" + "Источник: " + Source.getStringSource(news.getSource()), image);
                    } else {
                        sendMessage(chatId, "Все новости с данным тэгом были показаны");
                    }
                } else {
                    sendMessage(chatId, "Данного тэга не существует");
                }
                isTagSearchActive = false;
            } else {
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
                    case "/news":
                        newsMenu(chatId);
                        break;
                    case "Популярные валюты":
                        topValute(chatId, inlineKeyboardService.getTopValuteKeyboard());
                        checkKeyboard = true;
                        break;
                    case "Все валюты":
                        allValute(chatId, inlineKeyboardService.getAllValuteKeyboard());
                        checkKeyboard = false;
                        break;
                    case "Популярные тэги":
                        topTags(chatId, inlineKeyboardService.getTopTagsKeyboard());
                        break;
                    case "Поиск по тэгу":
                        sendMessage(chatId, "Напишите название тэга");
                        isTagSearchActive = true;
                        tagSearchChatId = chatId;
                        break;
                    default:
                        sendMessage(chatId, "Sorry,command is not available");
                }
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (data.startsWith("currency")) {
                int messageId = update.getCallbackQuery().getMessage().getMessageId();
                Currency currency = currencyService.findByCodeValute(data.substring("currency_".length()));
                String text = "Курс " + currency.getName() + " (" + currency.getCharCode() + ") = " + currency.getValue() + " руб";
                if (checkKeyboard.equals(true)) {
                    updateMessageText(chatId, messageId, text, inlineKeyboardService.getTopValuteKeyboard());
                } else {
                    updateMessageText(chatId, messageId, text, inlineKeyboardService.getAllValuteKeyboard());
                }
            } else if (data.startsWith("tag")) {
                News news = newsService.getNewsByTagForUser(chatId, data.substring("tag_".length()));
                if (news != null) {
                    File image = imageService.saveImage(news.getImageUrl());
                    sendPhotoMessage(chatId, news.getTitle()
                            + "\n" + "\n" + news.getMainText()
                            + "\n" + "\n" + "Источник: " + Source.getStringSource(news.getSource()), image);
                } else {
                    sendMessage(chatId, "Данного тэга не существует");
                }
            }
        }
    }

    private void topTags(long chatId, InlineKeyboardMarkup topTagsKeyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Популярные тэги");

        message.setReplyMarkup(topTagsKeyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void newsMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Новости");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("Популярные тэги");
        row.add("Поиск по тэгу");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("error");
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

    private void allValute(long chatId, InlineKeyboardMarkup allValuteKeyboard) {
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
        String answer = "Приветствуем, " + firstName + " , в чат-бот агрегаторе новостей в сфере финансы";
        log.info("Replied to user " + firstName);
        sendMessage(chatId, answer);
    }

    protected void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    protected void sendPhotoMessage(long chatId, String text, File image) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        if (image != null) {
            sendPhoto.setPhoto(new InputFile(image));
            try {
                execute(sendPhoto);
            } catch (TelegramApiRequestException error) {
                if (error.getErrorCode() == 403) {
                    log.info("Пользователь " + chatId + " заблокировал бота");
                    return;
                }
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки картинки", e);
            }
        } else {
            log.info("Нет картинки для добавления или произошла ошибка");
        }
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiRequestException error) {
            if (error.getErrorCode() == 403) {
                log.info("Пользователь " + chatId + " заблокировал бота");
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
