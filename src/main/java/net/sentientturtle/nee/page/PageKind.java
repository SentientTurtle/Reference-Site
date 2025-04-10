package net.sentientturtle.nee.page;

import net.sentientturtle.html.Frame;
import net.sentientturtle.html.HTMLUtil;
import net.sentientturtle.nee.data.sde.SDEData;
import net.sentientturtle.nee.data.datatypes.MapItem;
import net.sentientturtle.nee.data.datatypes.Cluster;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Enum containing the various types of {@link Page}s
 */
public enum PageKind {
    CATEGORY(data ->
        data.getCategories()
            .values()
            .stream()
            .map(CategoryPage::new)
    ),
    GROUP(data ->
        data.getGroups()
            .values()
            .stream()
            .map(GroupPage::new)
    ),
    TYPE(data ->
        data.getTypes()
            .values().stream()
            .filter(type -> type.groupID != 15)
            .map(TypePage::new)
    ),
    Comparison(data ->
        data.getParentTypeMap().values()
            .stream()
            .distinct()
            .map(typeID -> data.getTypes().get(typeID))
            .map(ComparisonPage::new)
    ),
    ITEM_TREE(_ -> Stream.of(new ShipTreePage(), new StructureTreePage())),
    MARKET_GROUP(data ->
        data.getMarketGroups()
            .values()
            .stream()
            .map(MarketGroupPage::new)
    ),
    MAP(
        dataSupplier -> Stream.of(
                Stream.of(Cluster.K_SPACE, Cluster.W_SPACE),
                dataSupplier.getRegions().values().stream(),
                dataSupplier.getConstellations().values().stream(),
                dataSupplier.getSolarSystems().values().stream()
            )
            // Load-bearing cast, otherwise Java infers Stream<?>, and we cannot cast ? to MapItem in the final stage.
            .flatMap((Function<Stream<? extends MapItem>, Stream<? extends MapItem>>) stream -> stream)
            .map(MapPage::new)
    ),
    STATIC(_ -> Stream.of(new IndexPage(), new SearchResults(), new DynamicMapPage(), new TermsOfServicePage(), new DevResourcePage(), new SettingsPage())) {
        @Override
        public String getPageFilePath(String pageName) {
            return pageName + ".html";
        }

        @Override
        public int getFolderDepth() {
            return 0;
        }
    };

    public final Function<SDEData, Stream<Frame>> streamSupplier;

    /**
     * @param streamSupplier Function that returns a stream of all pages of the type, using a specified data supplier.
     */
    PageKind(Function<SDEData, Stream<Frame>> streamSupplier) {
        this.streamSupplier = streamSupplier;
    }

    /**
     * Utility method to get a single stream containing all pages that can be generated.
     *
     * @param SDEData Data supplier to use.
     * @return Stream containing all pages that can be generated.
     */
    public static Stream<Frame> pageStream(SDEData SDEData) {
        return Arrays.stream(values()).flatMap((Function<PageKind, Stream<Frame>>) pageType -> pageType.streamSupplier.apply(SDEData));
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    /**
     * Returns the file path for a page of this type with the given name
     *
     * @param pageName Name for the page
     * @return File path for the given page name
     */
    public String getPageFilePath(String pageName) {
        return getFileFolder() + "/" + HTMLUtil.escapeFileNameURL(pageName) + ".html";
    }

    /**
     * Returns the file path for a page of this type with the given name
     *
     * @param pageName Name for the page
     * @return File path for the given page name
     */
    public String getPageURLPath(String pageName) {
        return HTMLUtil.urlQuasiEscape(getFileFolder()) + "/" + HTMLUtil.urlQuasiEscape(HTMLUtil.escapeFileNameURL(pageName)) + ".html";
    }

    /**
     * @return The output folder for this PageKind
     */
    public String getFileFolder() {
        return this.name().toLowerCase();
    }

    public int getFolderDepth() {
        return 1;
    }
}