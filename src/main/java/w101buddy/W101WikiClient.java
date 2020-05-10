package w101buddy;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class W101WikiClient {
    private static final String WIKI_URL = "https://www.wizard101central.com/wiki/";
    private static final String FAILED_REQUEST = "Request to Wizard101 Central failed!\nCheck your internet connection and try again.";
    private static final String PAGE_DOES_NOT_EXIST = "Wiki page does not exist, did you misspell anything?";

    enum PageType {
        Reagent,
    }

    public static void main(String[] args) {
        String aether = getWikiPageOrErrorMessage("Aether", PageType.Reagent);
        System.out.println("Aether should exist: " + String.valueOf(!aether.equals(PAGE_DOES_NOT_EXIST) && !aether.equals(FAILED_REQUEST)));
        String formattedBlackLotus = getWikiPageOrErrorMessage("Black Lotus", PageType.Reagent);
        System.out.println("Black Lotus should exist: " + String.valueOf(!formattedBlackLotus.equals(PAGE_DOES_NOT_EXIST) && !formattedBlackLotus.equals(FAILED_REQUEST)));
        String blackLotus = getWikiPageOrErrorMessage(" black  lotus   ", PageType.Reagent);
        System.out.println("Black lotus should exist: " + String.valueOf(!blackLotus.equals(PAGE_DOES_NOT_EXIST) && !blackLotus.equals(FAILED_REQUEST)));
        String misspelled = getWikiPageOrErrorMessage("Black a", PageType.Reagent);
        System.out.println("Black a should error: " + misspelled.equals(PAGE_DOES_NOT_EXIST));
    }

    static String getWikiPageOrErrorMessage(String term, PageType pageType) {
        String formattedTerm = formatTerm(term);
        try {
            Document wikiPage = Jsoup.connect(WIKI_URL + pageType.name() + ":" + formattedTerm).get();
            if (isEmptyResult(wikiPage)) {
                return PAGE_DOES_NOT_EXIST;
            }
            return extractInformation(wikiPage);
        } catch (HttpStatusException e) {
            return e.getStatusCode() == 404 ? PAGE_DOES_NOT_EXIST : FAILED_REQUEST;
        } catch (IOException e) {
            return FAILED_REQUEST;
        }
    }

    private static String formatTerm(String term) {
        String[] words = term.trim().split("\\s+");
        return Arrays.stream(words)
            .filter(word -> !word.isEmpty())
            .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
            .collect(Collectors.joining("_"));
    }

    private static boolean isEmptyResult(Document result) {
        // this is a fail-safe in case the page does not exist page is returned instead of a 404
        Element noResultDiv = result.getElementById("noarticletext");
        return noResultDiv != null;
    }

    private static String extractInformation(Document wikiPage) {
        return ""; // TODO
    }
}
