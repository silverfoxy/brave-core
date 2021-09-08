package org.chromium.chrome.browser.brave_news.models;

import java.util.HashMap;
import org.chromium.brave_news.mojom.Publisher;

public class PublisherCategory {

    public String categoryName;
    public Publisher publisher;

    public PublisherCategory() {}

    public PublisherCategory(String categoryName, Publisher publisher) {
        this.categoryName = categoryName;
        this.publisher = publisher;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "PublisherCategory{" +
                "categoryName='" + categoryName + '\'' +
                ", publisher=" + publisher +
                '}';
    }
}
