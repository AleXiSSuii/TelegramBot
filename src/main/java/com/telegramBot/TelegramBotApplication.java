package com.telegramBot;

import com.telegramBot.parsers.currency.Currency;
import com.telegramBot.parsers.currency.CurrencyService;
import com.telegramBot.parsers.news.NewsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class TelegramBotApplication {

	public static void main(String[] args) throws IOException {

		ConfigurableApplicationContext context = SpringApplication.run(TelegramBotApplication.class, args);

		CurrencyService currencyService = context.getBean(CurrencyService.class);
		NewsService newsService = context.getBean(NewsService.class);

		List<Currency> currencies = currencyService.parseCurrency();
		currencyService.saveCurrencyInDataBase(currencies);
		newsService.saveNewsInDataBase(newsService.parseNewNews());

		context.close();
	}

}
