package w101buddy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Optional;

public class W101WikiClient {
    private static final String WIKI_URL = "https://www.wizard101central.com/wiki/";

    enum PageType {
        Reagent,
    }

    public static void main(String[] args) throws IOException {
        Optional<String> aether = getWikiPage("Aether", PageType.Reagent);
        assert aether.isPresent();
        Optional<String> formattedBlackLotus = getWikiPage("Black_Lotus", PageType.Reagent);
        assert formattedBlackLotus.isPresent();
        Optional<String> blackLotus = getWikiPage(" Black lotus   ", PageType.Reagent);
        assert blackLotus.isPresent();
        Optional<String> misspelled = getWikiPage("Black lots", PageType.Reagent);
        assert !misspelled.isPresent();
    }

    static Optional<String> getWikiPage(String term, PageType pageType) throws IOException {
        String formattedTerm = formatTerm(term);
        Document wikiPage = Jsoup.connect(WIKI_URL + pageType.name() + ":" + formattedTerm).get();
        if (isEmptyResult(wikiPage)) {
            return Optional.empty();
        }
        return Optional.of(extractInformation(wikiPage));
    }

    private static String formatTerm(String term) {
        // TODO add capitalization of letter after spaces
        // TODO replace any number of white space with regex
        return term.trim().replace(" ", "_");
    }

    private static boolean isEmptyResult(Document result) {
        Element noResultDiv = result.getElementById("noarticletext");
        return noResultDiv == null;
    }

    private static String extractInformation(Document wikiPage) {
        return ""; // TODO
    }
}
