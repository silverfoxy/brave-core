package org.chromium.chrome.browser.brave_news.models;

import java.util.HashMap;

public class NewsItem {
    String category;
    String publish_time;
    String url;
    String title;
    String description;
    String content_type;
    String publisher_id;
    String publisher_name;
    String creative_instance_id;
    String url_hash;
    String padded_img;
    String score;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPublish_time() {
        return publish_time;
    }

    public void setPublish_time(String publish_time) {
        this.publish_time = publish_time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public String getPublisher_id() {
        return publisher_id;
    }

    public void setPublisher_id(String publisher_id) {
        this.publisher_id = publisher_id;
    }

    public String getPublisher_name() {
        return publisher_name;
    }

    public void setPublisher_name(String publisher_name) {
        this.publisher_name = publisher_name;
    }

    public String getCreative_instance_id() {
        return creative_instance_id;
    }

    public void setCreative_instance_id(String creative_instance_id) {
        this.creative_instance_id = creative_instance_id;
    }

    public String getUrl_hash() {
        return url_hash;
    }

    public void setUrl_hash(String url_hash) {
        this.url_hash = url_hash;
    }

    public String getPadded_img() {
        return padded_img;
    }

    public void setPadded_img(String padded_img) {
        this.padded_img = padded_img;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }


    @Override
    public String toString() {
        return "NewsItem{" +
                "category='" + category + '\'' +
                ", publishTime='" + publish_time + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", content_type='" + content_type + '\'' +
                ", publisher_id='" + publisher_id + '\'' +
                ", publisher_name='" + publisher_name + '\'' +
                ", creative_instance_id='" + creative_instance_id + '\'' +
                ", url_hash='" + url_hash + '\'' +
                ", padded_img='" + padded_img + '\'' +
                ", score=" + score +
                '}';
    }

    public NewsItem() {
    }

    public NewsItem(String category, String publish_time, String url, String title, String description, String content_type, String publisher_id, String publisher_name, String creative_instance_id, String url_hash, String padded_img, String score) {
        this.category = category;
        this.publish_time = publish_time;
        this.url = url;
        this.title = title;
        this.description = description;
        this.content_type = content_type;
        this.publisher_id = publisher_id;
        this.publisher_name = publisher_name;
        this.creative_instance_id = creative_instance_id;
        this.url_hash = url_hash;
        this.padded_img = padded_img;
        this.score = score;
    }

    HashMap<String, String> vars = new HashMap<String, String>();

    public HashMap<String, String> getVars() {
        return vars;
    }

    public void setVars(HashMap<String, String> vars) {
        this.vars = vars;
    }

    public void setItemValue(String key, String value) {
        switch (key) {
            case "category":
                this.category = value;
                break;
            case "publish_time":
                this.publish_time = value;
                break;
            case "url":
                this.url = value;
                break;
            case "title":
                this.title = value;
                break;
            case "description":
                this.description = value;
                break;
            case "content_type":
                this.content_type = value;
                break;
            case "publisher_id":
                this.publisher_id = value;
                break;
            case "publisher_name":
                this.publisher_name = value;
                break;
            case "creative_instance_id":
                this.creative_instance_id = value;
                break;
            case "url_hash":
                this.url_hash = value;
                break;
            case "padded_img":
                this.padded_img = value;
                break;
            case "score":
                this.score = value;
                break;
        }
    }
}
