import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Parse {

    private static final String REGEX_BY_LINES = "\\r?\\n";
    private static final String REGEX_BY_WORDS = "[^\\w/]+";
    private static final String DECIMAL_FORMAT = "#.###";
    private static final String REGEX_SEARCH_FOR_NUMBER = "-?\\d+(\\.\\d+)?";
    private static final String REGEX_SEARCH_FOR_FRACTION = "-?\\d+/(\\d+)?";
    private static final float THOUSAND = 1000;
    private static final float MILLION = 1000000;
    private static final float BILLION = 1000000000;


    private boolean isFraction(String strNum) {
        Pattern pattern = Pattern.compile(REGEX_SEARCH_FOR_FRACTION);
        return pattern.matcher(strNum).matches();
    }

    private boolean isNumber(String strNum) {
        if (strNum == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(REGEX_SEARCH_FOR_NUMBER);
        return pattern.matcher(strNum).matches() || isFraction(strNum);
    }

    float parseFraction(String ratio) {
        if (ratio.contains("/")) {
            String[] rat = ratio.split("/");
            return Float.parseFloat(rat[0]) / Float.parseFloat(rat[1]);
        } else {
            return Float.parseFloat(ratio);
        }
    }

    private String handleBigNumbers(String number) {
        DecimalFormat df = new DecimalFormat(DECIMAL_FORMAT);
        float parsedNumber = parseFraction(number);
        String formattedNumber;
        if (parsedNumber >= THOUSAND) {
            if (parsedNumber >= MILLION) {
                if (parsedNumber >= BILLION) {
                    formattedNumber = df.format(parsedNumber / BILLION);
                    return formattedNumber + "B";
                }
                formattedNumber = df.format(parsedNumber / MILLION);
                return formattedNumber + "M";
            }
            formattedNumber = df.format(parsedNumber / THOUSAND);
            return formattedNumber + "K";
        }
        return number;
    }

    private boolean isPercent(String precentage) {
        return "percent".equals(precentage) || "percentage".equals(precentage);
    }

    private String handleDollarCases(String line) {
        ArrayList<String> words = new ArrayList<>(Arrays.asList(line.split("\\s+")));
        StringBuilder parsedLine = new StringBuilder();
        for (int i = 0; i < words.size(); ++i) {
            String word = words.get(i);
            if (word.contains("$")) {
                parsedLine.append(word.replace("$", "")).append(" Dollar");
            } else {
                if (i != 0) {
                    parsedLine.append(" ");
                }
                parsedLine.append(word);
            }
        }
        return parsedLine.toString();
    }

    private String convertNumberFromTextToChar(String bigNumber) {
        if ("Thousand".equals(bigNumber) || "Thousand".toLowerCase().equals(bigNumber) || "Thousand".toUpperCase().equals(bigNumber)) {
            return "K";
        } else if ("Million".equals(bigNumber) || "Million".toLowerCase().equals(bigNumber) || "Million".toUpperCase().equals(bigNumber)) {
            return "M";
        } else if ("Billion".equals(bigNumber) || "Billion".toLowerCase().equals(bigNumber) || "Billion".toUpperCase().equals(bigNumber)) {
            return "B";
        }
        return null;
    }

    private ArrayList<String> parseLine(String line) {
        ArrayList<String> parsedWords = new ArrayList<>();
        ArrayList<String> words = new ArrayList<>(Arrays.asList(line.split(REGEX_BY_WORDS)));
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i).replaceAll(",", "");
            if (word.length() > 1) {
                if (isNumber(word)) {
                    try {
                        String nextWord = words.get(i + 1).replaceAll(",", "");
                        String character = convertNumberFromTextToChar(nextWord);
                        if (isPercent(nextWord)) {
                            parsedWords.add(word + "%");
                            i++;
                        } else if (character != null) {
                            parsedWords.add(word + character);
                            i++;
                        } else {
                            parsedWords.add(handleBigNumbers(word));
                        }
                    } catch (Exception e) {
                        parsedWords.add(handleBigNumbers(word));
                    }
                } else {
                    parsedWords.add(word);
                }
            }
        }

        return parsedWords;
    }

    public ArrayList<String> parse(Article article) {
        ArrayList<String> articleLines = new ArrayList<>(Arrays.asList(article.getContent().split(REGEX_BY_LINES)));
        ArrayList<String> parsedWords = new ArrayList<>();
        for (String line : articleLines) {
            line = handleDollarCases(line);
            parsedWords.addAll(parseLine(line));
        }
        return parsedWords;
    }
}
