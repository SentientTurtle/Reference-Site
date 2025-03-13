package net.sentientturtle.html;

/// Interface for pages with a persistent URL; Item names may change
public interface HasPersistentUrl extends Document {
    /// Persistent name for this page, as url segment. Does not have to include a file extension
    String persistentName();

    /**
     * @return The URL path that is persistent for this page
     */
    default String getPersistentURL() {
        return HTMLUtil.urlQuasiEscape(this.getPageKind().getFileFolder()) + "/" + HTMLUtil.urlQuasiEscape(persistentName());
    }
}
