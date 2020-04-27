package w101buddy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class W101WikiClient {
    private static final String WIKI_URL = "https://www.wizard101central.com/wiki/";

    public static void main(String[] args) {

    }

    public void searchOrGetWikiPage() {

    }

    public void getWikiPage(String term, PageType pageType) throws IOException {
        Document wikiPage = Jsoup.connect(WIKI_URL + pageType.name() + ":" + term).get();
    }
}
