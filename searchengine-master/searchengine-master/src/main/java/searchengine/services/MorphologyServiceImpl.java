package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MorphologyServiceImpl implements MorphologyService {
    private final LuceneMorphology luceneMorphology;
    private static final String[] PARTICLES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};

    public List<String> morphologyForms(String word) {
        if (!luceneMorphology.checkString(word)) {
            return Collections.emptyList();
        }
        return luceneMorphology.getMorphInfo(word);
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : PARTICLES) {
            String baseProperty = wordBase.split("\\s+", 2)[1];
            if (baseProperty.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private List<String> listOfAllText(String html) {
        String text = Jsoup.clean(html, Whitelist.none());
        text = Parser.unescapeEntities(text, false)
                .replaceAll("([^A-Яа-я\\s])", " ");

        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("[A-Яа-я]+");
        Matcher matcher = pattern.matcher(text);
        int end = 0;
        while (matcher.find()) {
            int start = matcher.start();
            if (start > end) {
                words.add(text.substring(end, start));
            }
            words.add(matcher.group());
            end = start + matcher.group().length();
        }
        return words;
    }

    private List<String> getNormalWords(String word) {
        List<String> wordBaseForms = morphologyForms(word);
        if (wordBaseForms.isEmpty()
                || anyWordBaseBelongToParticle(wordBaseForms)) {
            return Collections.emptyList();
        }
        return luceneMorphology.getNormalForms(word);
    }

    private boolean containsAny(List<String> lemmas, List<String> words) {
        for (String lemma : lemmas) {
            if (words.contains(lemma)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getLemmaSet(String html) {
        return collectLemmas(html).keySet();
    }

    @Override
    public String getSnippet(String html, List<String> lemmas, int snippetSize) {
        List<String> text = listOfAllText(html);
        int[] lemmasMap = new int[text.size()];

        int startIndex = 0;
        int sum = 0;
        int maxStartIndex = 0;
        int maxSum = 0;
        for (int i = 0; i < text.size(); i++) {
            List<String> words = getNormalWords(text.get(i).toLowerCase());
            if (words != null && containsAny(lemmas, words)) {
                lemmasMap[i] = 1;
                sum++;
                text.set(i, "<b>" + text.get(i) + "</b>");
            }
            if ((i >= startIndex + snippetSize) && (i <= text.size() - snippetSize)) {
                sum -= lemmasMap[startIndex++];
            }
            if (sum > maxSum) {
                maxStartIndex = startIndex;
                maxSum = sum;
            }
        }

        List<String> resultText = maxStartIndex + snippetSize > text.size() ?
                text.subList(maxStartIndex, text.size()) :
                text.subList(maxStartIndex, maxStartIndex + snippetSize);
        StringBuilder snippet = maxStartIndex > 0 ?
                new StringBuilder("...") : new StringBuilder();
        resultText.forEach(snippet::append);
        return maxStartIndex < text.size() - snippetSize ?
                snippet.append("...").toString() : snippet.toString();
    }

    @Override
    public Map<String, Integer> collectLemmas(String html) {
        String[] text = arrayContainRussianWords(html);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for (String word : text) {
            List<String> words = getNormalWords(word);
            words.forEach(lemma -> {
                if (lemmas.putIfAbsent(lemma, 1) != null) {
                    lemmas.compute(lemma, (key, value) -> value + 1);
                }
            });
        }
        return lemmas;
    }

    private String[] arrayContainRussianWords(String html) {
        String text = Jsoup.parse(html).text();
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }
}
