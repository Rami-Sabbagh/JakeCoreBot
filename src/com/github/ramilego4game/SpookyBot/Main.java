package com.github.ramilego4game.SpookyBot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        DefaultBotOptions options = new DefaultBotOptions();
        options.setMaxThreads(10);

        try {
            botsApi.registerBot(new JakeCoreBot(getBotToken(), "JakeCoreBot", options));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static String getBotToken() {
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("token.txt");
        assert inputStream != null : "Please place the bot token in \"resources/token.txt\"";

        Scanner scanner = new Scanner(inputStream);
        String token = scanner.next();
        scanner.close();

        return token;
    }
}
