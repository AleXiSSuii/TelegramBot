package com.telegramBot.model;

import com.telegramBot.model.enums.Source;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
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
    @Column(name = "image_url")
    private String imageUrl;
    @ManyToMany(mappedBy = "news", fetch = FetchType.EAGER)
    private Set<Tag> tags = new HashSet<>();
    @Column(name ="check_send")
    private Boolean checkSend;
    @Column(name = "source")
    @Enumerated(value = EnumType.STRING)
    private Source source;
    @ManyToMany(mappedBy = "seenNews", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
}
