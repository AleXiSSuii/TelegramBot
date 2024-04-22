package com.telegramBot.service;

import com.telegramBot.model.Currency;
import com.telegramBot.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.telegramBot.utils.Constants.*;

@Service
public final class InlineKeyboardService{

    @Autowired
    private CurrencyRepository currencyRepository;

    public InlineKeyboardMarkup getTopValuteKeyboard(){
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineOne = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineTwo = new ArrayList<>();
        InlineKeyboardButton usdButton = new InlineKeyboardButton();
        usdButton.setText(USD);
        usdButton.setCallbackData("USD");
        InlineKeyboardButton eurButton = new InlineKeyboardButton();
        eurButton.setText(EUR);
        eurButton.setCallbackData("EUR");
        InlineKeyboardButton gbpButton = new InlineKeyboardButton();
        gbpButton.setText(GBP);
        gbpButton.setCallbackData("GBP");
        InlineKeyboardButton cnyButton = new InlineKeyboardButton();
        cnyButton.setText(CNY);
        cnyButton.setCallbackData("CNY");
        InlineKeyboardButton uahButton = new InlineKeyboardButton();
        uahButton.setText(UAH);
        uahButton.setCallbackData("UAH");
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
    public InlineKeyboardMarkup getAllValuteKeyboard(){
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<Currency> currencies = currencyRepository.findAll();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        for (Currency currency:currencies){
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(currency.getCode());
            button.setCallbackData(currency.getCode());
            rowInLine.add(button);
            if(rowInLine.size()==3){
                System.out.println(rowInLine);
                rowsInLine.add(rowInLine);
                rowInLine = new ArrayList<>();
            }
        }
        markupInLine.setKeyboard(rowsInLine);

        return markupInLine;
    }

}
