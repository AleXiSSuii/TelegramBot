package com.telegramBot.service;

import com.telegramBot.model.Currency;
import com.telegramBot.model.Tag;
import com.telegramBot.repository.CurrencyRepository;
import com.telegramBot.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.telegramBot.utils.Constants.*;

@Service
public final class InlineKeyboardService {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private TagRepository tagRepository;

    public InlineKeyboardMarkup getTopValuteKeyboard() {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineOne = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineTwo = new ArrayList<>();
        InlineKeyboardButton usdButton = new InlineKeyboardButton();
        usdButton.setText(USD);
        usdButton.setCallbackData("currency_USD");
        InlineKeyboardButton eurButton = new InlineKeyboardButton();
        eurButton.setText(EUR);
        eurButton.setCallbackData("currency_EUR");
        InlineKeyboardButton gbpButton = new InlineKeyboardButton();
        gbpButton.setText(GBP);
        gbpButton.setCallbackData("currency_GBP");
        InlineKeyboardButton cnyButton = new InlineKeyboardButton();
        cnyButton.setText(CNY);
        cnyButton.setCallbackData("currency_CNY");
        InlineKeyboardButton uahButton = new InlineKeyboardButton();
        uahButton.setText(UAH);
        uahButton.setCallbackData("currency_UAH");
        rowInLineOne.add(usdButton);
        rowInLineOne.add(eurButton);
        rowInLineOne.add(gbpButton);
        rowInLineTwo.add(cnyButton);
        rowInLineTwo.add(uahButton);

        rowsInLine.add(rowInLineOne);
        rowsInLine.add(rowInLineTwo);
        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    public InlineKeyboardMarkup getAllValuteKeyboard() {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<Currency> currencies = currencyRepository.findAll();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        for (Currency currency : currencies) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(currency.getCharCode());
            button.setCallbackData("currency_" + currency.getCharCode());
            rowInLine.add(button);
            if (rowInLine.size() == 3) {
                rowsInLine.add(rowInLine);
                rowInLine = new ArrayList<>();
            }
        }
        markupInLine.setKeyboard(rowsInLine);

        return markupInLine;
    }

    public InlineKeyboardMarkup getTopTagsKeyboard() {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        List<Tag> tags = tagRepository.findTopTagsByNewsCount();
        System.out.println(tags);
        int counterTag = 0;
        for (Tag tag : tags) {
            if (counterTag > 12) {
                break;
            }
            if (tag.getTitle().length() < 10) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(tag.getTitle());
                button.setCallbackData("tag_" + tag.getTitle());
                rowInLine.add(button);
                if (rowInLine.size() == 3) {
                    rowsInLine.add(rowInLine);
                    rowInLine = new ArrayList<>();
                }
                counterTag++;
            }

        }
        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

}
