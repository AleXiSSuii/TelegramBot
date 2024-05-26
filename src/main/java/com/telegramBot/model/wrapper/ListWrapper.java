package com.telegramBot.model.wrapper;

import com.telegramBot.model.News;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListWrapper{
    private List<News> newsList;
    private List<String> tagsLine;

}
