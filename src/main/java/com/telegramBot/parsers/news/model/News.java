package com.telegramBot.parsers.news.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "link")
    private String link;
    @Column(name = "title")
    private String title;
    @Column(name = "date_time")
    private LocalDateTime dateTime;
    @Column(name = "main_text")
    private String mainText;
    @ManyToMany(mappedBy = "news", fetch = FetchType.EAGER)
    private List<Tag> list = new ArrayList<>();
}
