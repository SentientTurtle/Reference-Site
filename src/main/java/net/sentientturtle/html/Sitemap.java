package net.sentientturtle.html;

import java.util.concurrent.atomic.AtomicBoolean;

public class Sitemap {
    private final StringBuilder builder;    // Class itself provides synchronization, so Builder instead of Buffer
    private String document;

    public Sitemap() {
        this.builder = new StringBuilder();
        this.document = null;

        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
    }

    public synchronized void addSite(String fullUrl) {
        if (this.document != null) throw new IllegalStateException("Sitemap already built, cannot append new value!");
        builder.append("<url><loc>").append(fullUrl).append("</loc></url>\n");
    }

    public synchronized String getDocumentString() {
        if (document == null) {
            builder.append("</urlset>");
            document = builder.toString();
        }

        return document;
    }
}
