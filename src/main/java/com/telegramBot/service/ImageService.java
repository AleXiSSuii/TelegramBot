package com.telegramBot.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    public File saveImage(String urlImage){
        try {
            URL imageUrl = new URL(urlImage);
            InputStream inputStream = new BufferedInputStream(imageUrl.openStream());
            File targetDir = new File("D:\\Бот Агрегатор\\TelegramBot\\src\\main\\resources\\newsPicture");

            String filename = UUID.randomUUID() + ".jpg";
            File imageFile = new File(targetDir, filename);
            IOUtils.copy(inputStream, new FileOutputStream(imageFile));
            return imageFile;
        }catch (IOException e) {
            log.error("Ошибка сохранении картинки");
        }
        return null;
    }
}
