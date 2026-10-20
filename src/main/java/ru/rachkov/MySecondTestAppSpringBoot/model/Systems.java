package ru.rachkov.MySecondTestAppSpringBoot.model;

import lombok.Getter;

@Getter
public enum Systems {

    ERP("Enterprise Resource Planning"),
    CRM("Customer Relationship Management"),
    WMS("Warehouse Management System");

    private final String description;

    Systems(String description) {
        this.description = description;
    }
}
