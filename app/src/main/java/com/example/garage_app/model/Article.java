package com.example.garage_app.model;

public class Article {
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private Source source;

    // Getters și Setters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public String getUrlToImage() { return urlToImage; }

    public static class Source {
        private String id;
        private String name;

        // Getters și Setters
    }
}

