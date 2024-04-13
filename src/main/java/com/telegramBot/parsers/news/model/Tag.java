package com.telegramBot.parsers.news.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tag")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "title", unique = true)
    private String title;
    @ManyToMany(cascade = CascadeType.REMOVE,fetch = FetchType.EAGER)
    private List<News> news = new ArrayList<>();
}