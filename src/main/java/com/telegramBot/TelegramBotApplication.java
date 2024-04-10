package com.telegramBot;

import com.telegramBot.parsers.Currency;
import com.telegramBot.parsers.CurrencyService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

@SpringBootApplication
public class TelegramBotApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(TelegramBotApplication.class, args);

		// Get the CurrencyService bean using the context
		CurrencyService currencyService = context.getBean(CurrencyService.class);

		List<Currency> currencies = currencyService.parseCurrency();
		currencyService.saveCurrencyInDataBase(currencies);

		// Close the context when finished (optional but recommended)
		context.close();
	}

}
