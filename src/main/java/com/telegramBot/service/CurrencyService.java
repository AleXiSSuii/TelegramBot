package com.telegramBot.service;

import com.google.gson.Gson;

import com.telegramBot.model.Currency;
import com.telegramBot.repository.CurrencyRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    public List<Currency> parseCurrency(){
        List<Currency> currencies = new ArrayList<>();
        try{
            String json = new String(new URL("https://www.cbr-xml-daily.ru/daily_json.js")
                    .openStream()
                    .readAllBytes());
            JSONObject currenciesData = new JSONObject(json)
                    .getJSONObject("Valute");
            currencies = currenciesData.keySet()
                    .stream()
                    .map((currency) -> new Gson().fromJson(
                            currenciesData.getJSONObject(currency).toString(),
                            Currency.class)).toList();
        } catch (Exception ignored){

        }
        return currencies;
    }

    public void saveCurrencyInDataBase(List<Currency> currencies){
        for(Currency currency:currencies){
            currencyRepository.save(currency);
        }
    }

    public void updateCurrency(List<Currency> currencies){
        List<Currency> oldCurrencies = currencyRepository.findAll();
        for(Currency currency:currencies){
            Currency existingCurrency = oldCurrencies.stream()
                    .filter(c -> c.getCharCode().equals(currency.getCharCode()))
                    .findFirst().orElse(null);
            if (existingCurrency != null) {
                existingCurrency.setValue(currency.getValue());
                currencyRepository.save(existingCurrency);
            }else {
                currencyRepository.save(currency);
            }
        }
    }

    public Currency findByCodeValute(String code){
        return currencyRepository.findByCharCode(code);
    }
}
