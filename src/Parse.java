import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Parse {

    private static final String REGEX_BY_LINES = "\\r?\\n";
    private static final String REGEX_BY_WORDS = "[^\\w/-]+";
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

    private float parseFraction(String ratio) {
        if (ratio.contains("/")) {
            String[] rat = ratio.split("/");
            return Float.parseFloat(rat[0]) / Float.parseFloat(rat[1]);
        } else {
            return Float.parseFloat(ratio);
        }
    }

    private String parseNumber(String number) {
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

    private String convertMonthToNumber(String month){
        if("January".equalsIgnoreCase(month) || "JAN".equalsIgnoreCase(month)){
            return "01";
        }
        if("February".equalsIgnoreCase(month) || "FEB".equalsIgnoreCase(month)){
            return "02";
        }
        if("March".equalsIgnoreCase(month) || "MAR".equalsIgnoreCase(month)){
            return "03";
        }
        if("April".equalsIgnoreCase(month) || "APR".equalsIgnoreCase(month)){
            return "04";
        }
        if("May".equalsIgnoreCase(month)){
            return "05";
        }
        if("June".equalsIgnoreCase(month) || "JUN".equalsIgnoreCase(month)){
            return "06";
        }
        if("July".equalsIgnoreCase(month) || "JUL".equalsIgnoreCase(month)){
            return "07";
        }
        if("August".equalsIgnoreCase(month) || "AUG".equalsIgnoreCase(month)){
            return "08";
        }
        if("September".equalsIgnoreCase(month) || "SEP".equalsIgnoreCase(month)){
            return "09";
        }
        if("October".equalsIgnoreCase(month) || "OCT".equalsIgnoreCase(month)){
            return "10";
        }
        if("November".equalsIgnoreCase(month) || "NOV".equalsIgnoreCase(month)){
            return "11";
        }
        if("December".equalsIgnoreCase(month) || "DEC".equalsIgnoreCase(month)){
            return "12";
        }
        return "";
    }


    private String parseDates(String datePart1, String datePart2) {

        if(!convertMonthToNumber(datePart1).equals("")){
            if(datePart2.length() == 1){ //covers the case of Month D
                return convertMonthToNumber(datePart1) + "-0" + datePart2;
            }
            if(datePart2.length() == 2){ //covers the case of Month DD
                return convertMonthToNumber(datePart1) + "-" + datePart2;
            }
            if(datePart2.length() == 4){ // covers the case of Month YYYY
                return datePart2 + "-" + convertMonthToNumber(datePart1);
            }
        }
        else{
            if(datePart1.length() == 1){ //covers the case of D Month
                return convertMonthToNumber(datePart2) + "-0" + datePart1;
            }
            else{ // covers the case of DD Month
                return convertMonthToNumber(datePart2) + "-" + datePart1;
            }
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
                            // TODO: Handle "number over million section";
                            parsedWords.add(word + character);
                            i++;
                        }else if (!convertMonthToNumber(nextWord).equals("")) { //covers cases of: DD MM
                            parseDates(word,nextWord);
                            i++;
                        }
                        else {
                            parsedWords.add(parseNumber(word));
                        }
                    } catch (Exception e) { // if there is no extra word after "word"
                        parsedWords.add(parseNumber(word));
                    }
                } else if (!convertMonthToNumber(word).equals("")){
                    try{
                        String nextWord = words.get(i + 1).replaceAll(",", "");
                        if(isNumber(nextWord)){
                            parseDates(word,nextWord);
                        }
                        else{
                            parsedWords.add(word);
                        }
                    }
                    catch (Exception e){
                        parsedWords.add(word);
                    }
                }
                else {
                    // TODO: Handle only "​Between number and number (for example: between 18 and 24)" section in range part
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
