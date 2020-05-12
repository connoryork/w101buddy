package w101buddy;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class W101WikiClient {
    private static final String WIKI_URL = "https://www.wizard101central.com/wiki/";
    private static final String FAILED_REQUEST = "Request to Wizard101 Central failed! Check your internet connection and try again.";
    private static final String PAGE_DOES_NOT_EXIST = "Wiki page does not exist. Did you misspell anything? Is the correct category selected?";
    private static final String FAILED_TO_PARSE = "W101Buddy failed to parse the returned data. Refer to https://github.com/connoryork/w101buddy.";

    // from http://www.wizard101central.com/wiki/Basic:Creating_New_Pages#Namespaces
    enum Namespace {
        BeastmoonForm,
        Creature,
        House,
        Item,
        ItemCard,
        Location,
        Minion,
        Mount,
        NPC,
        Pet,
        PetAbility,
        Polymorph,
        Quest,
        Reagent,
        Snack,
        Spell,
        TreasureCard,
    }

    public static void main(String[] args) {
        String aether = getWikiPageOrErrorMessage("Aether", Namespace.Reagent);
        System.out.println("Aether should exist: " + String.valueOf(!aether.equals(PAGE_DOES_NOT_EXIST) && !aether.equals(FAILED_REQUEST)));
        String formattedBlackLotus = getWikiPageOrErrorMessage("Black Lotus", Namespace.Reagent);
        System.out.println("Black Lotus should exist: " + String.valueOf(!formattedBlackLotus.equals(PAGE_DOES_NOT_EXIST) && !formattedBlackLotus.equals(FAILED_REQUEST)));
        String blackLotus = getWikiPageOrErrorMessage(" black  lotus   ", Namespace.Reagent);
        System.out.println("Black lotus should exist: " + String.valueOf(!blackLotus.equals(PAGE_DOES_NOT_EXIST) && !blackLotus.equals(FAILED_REQUEST)));
        String misspelled = getWikiPageOrErrorMessage("Black a", Namespace.Reagent);
        System.out.println("Black a should error: " + misspelled.equals(PAGE_DOES_NOT_EXIST));
    }

    static String getWikiPageOrErrorMessage(String term, Namespace namespace) {
        String formattedTerm = formatTerm(term);
        try {
            Document wikiPage = Jsoup.connect(WIKI_URL + namespace.name() + ":" + formattedTerm).get();
            if (isEmptyResult(wikiPage)) {
                return PAGE_DOES_NOT_EXIST;
            }
            return extractContent(wikiPage);
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
        // this is a fail-safe in case the 404 redirect page is returned instead of just 404
        Element noResultDiv = result.getElementById("noarticletext");
        return noResultDiv != null;
    }

    private static String extractContent(Document wikiPage) {
        try {
            Element content = wikiPage.getElementById("mw-content-text");
            content.getElementsByTag("img").stream()
                .map(Element::parent)
                .filter(parent -> parent.tagName().equalsIgnoreCase("a"))
                .forEach(Node::remove);
            content.children().stream()
                .filter(child -> child.tagName().equalsIgnoreCase("b")
                    || child.tagName().equalsIgnoreCase("p"))
                .forEach(Node::remove);
            return content.outerHtml();
        } catch (NullPointerException e) {
            return FAILED_TO_PARSE;
        }
    }
}
