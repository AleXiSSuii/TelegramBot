package com.telegramBot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "currency")
public class Currency{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "char_code")
    @SerializedName("CharCode")
    private String charCode;
    @Column(name = "name")
    @SerializedName("Name")
    private String name;
    @Column(name = "value")
    @SerializedName("Value")
    private Double value;
}

