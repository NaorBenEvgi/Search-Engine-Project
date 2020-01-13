package Indexing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for parsing all the words and terms in a document into a certain format, depends on what the term represents.
 */
public class Parse {

    private static final String REGEX_BY_LINES = "\\r?\\n";
    private static final String REGEX_BY_WORDS = "[^\\w,$/%-]+";
    private static final String DECIMAL_FORMAT = "#.###";
    private static final String REGEX_SEARCH_FOR_NUMBER = "-?\\d+(\\.\\d+)?";
    private static final String REGEX_SEARCH_FOR_FRACTION = "-?\\d+/(\\d+)?";
    private static final float THOUSAND = 1000;
    private static final float MILLION = 1000000;
    private static final float BILLION = 1000000000;
    protected HashSet<String> stopWords;
    private HashMap<String,Term> dictionary;
    private int termPositionInDocument;
    private boolean skipNextWord = false;


    public Parse(String corpusPath){
        stopWords = new HashSet<>();
        Path stopWordsPath = Paths.get(corpusPath).resolve("stop_words.txt");
        fillStopWords(stopWordsPath.toString());
        termPositionInDocument = 0;
        dictionary = new HashMap<>();
    }


    /**
     * Reads the stop words file and fills the stop words database
     * @param pathToStopWords the path to the stop words file
     */
    private void fillStopWords(String pathToStopWords){
        stopWords = new HashSet<>();
        try{
            BufferedReader stopWordsReader = new BufferedReader(new FileReader(pathToStopWords));
            String word;
            while((word = stopWordsReader.readLine()) != null){
                stopWords.add(word);
            }

            stopWordsReader.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Checks if a given word is a stop word
     * @param word the given word
     * @return true if the word is a stop word, false otherwise
     */
    private boolean isStopWord(String word){
        return stopWords.contains(word);
    }


    /**
     * Iterates over a line and removes all the stop words in it.
     * @param words the given article words
     * @return the line without stop words
     */
    private ArrayList<String> eliminateStopWords(ArrayList<String> words){
        ArrayList<String> lineWithoutStopWords = new ArrayList<>();
        for (String word : words) {
            if (!isStopWord(word)) {
                lineWithoutStopWords.add(word);
            }
        }
        return lineWithoutStopWords;
    }

    /**
     * Checks if a given string contains a numerical fraction
     * @param strNum the given string
     * @return true if it contains a fraction, false otherwise
     */
    private boolean isFraction(String strNum) {
        Pattern pattern = Pattern.compile(REGEX_SEARCH_FOR_FRACTION);
        return pattern.matcher(strNum).matches();
    }

    /**
     * Checks if a given string contains a number
     * @param strNum the given string
     * @return true if it contains a number, false otherwise
     */
    private boolean isNumber(String strNum) {
        if (strNum == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(REGEX_SEARCH_FOR_NUMBER);
        return pattern.matcher(strNum).matches() || isFraction(strNum);
    }

    /**
     * Returns the numerical value of a fraction that's contained in a given string
     * @param ratio the given string
     * @return the numerical value of the fraction
     */
    private float parseFraction(String ratio) {
        if (ratio.contains("/")) {
            String[] rat = ratio.split("/");
            if(rat.length == 1)
                return Float.parseFloat(rat[0]);
            return Float.parseFloat(rat[0]) / Float.parseFloat(rat[1]);
        }
        return Float.parseFloat(ratio);
    }

    /**
     * Checks the value of the number in a given string, and returns a string that is a parsed form of it, according to its magnitude.
     * @param number the number as a string
     * @return the parsed form of the number
     */
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

    /**
     * Checks if a given string is either the word "percent" or "percentage".
     * @param percentage the given string
     * @return true if it's one of the words above, false otherwise
     */
    private boolean isPercent(String percentage) {
        return "percent".equals(percentage) || "percentage".equals(percentage);
    }

    /**
     * Parses the words that contain the dollar sign in a given line.
     * @param words the given line
     * @return the line with the $'s replaced with "Dollars"
     */
    private ArrayList<String> handleDollarCases(ArrayList<String> words) {
        ArrayList<String> parsedWords = new ArrayList<>();
        for (int i = 0; i < words.size(); ++i) {
            String word = words.get(i);
            if (word.contains("$")) {
                try {
                    String nextWord = words.get(i+1);
                    if (nextWord.equalsIgnoreCase("million")) {
                        parsedWords.add(word.replace("$", ""));
                        parsedWords.add("M");
                        parsedWords.add("Dollars");
                        i++;
                    }
                    else if(nextWord.equalsIgnoreCase("billion")){
                        parsedWords.add(word.replace("$", "") + "000");
                        parsedWords.add("M");
                        parsedWords.add("Dollars");
                        i++;
                    }
                    else { //in case the number should stay as it is
                        parsedWords.add(word.replace("$", ""));
                        parsedWords.add("Dollars");
                    }
                }
                catch (Exception e){
                    parsedWords.add(word.replace("$", ""));
                    parsedWords.add("Dollars");
                }
            } else {
                parsedWords.add(word);
            }
        }
        return parsedWords;
    }

    /**
     * Checks if the line contains prices with number larger than million, and parses them accordingly
     * @param words the line to parse
     * @return the parsed line
     */
    private ArrayList<String> pricesOverMillion(ArrayList<String> words){
        ArrayList<String> parsedLine = new ArrayList<>();
        for (int i = 0; i < words.size(); ++i) {
            String word = words.get(i);
            if (word.endsWith("m") && isNumber(word.substring(0,word.length()-1))) { //#m Dollars -> # M Dollars
                parsedLine.add(word.replace("m", "")+"M");
            }
            else if (word.endsWith("bn") && isNumber(word.substring(0,word.length()-2))) { //#bn Dollars -> #000 M Dollars
                Double wordToMultiply = new Double(word.replace("bn", ""));
                wordToMultiply *= 1000;
                String multipliedWord = wordToMultiply.toString().substring(0,wordToMultiply.toString().length()-2);
                parsedLine.add(multipliedWord);
                parsedLine.add("M");
            }
            else if(isNumber(word)) { // # million / billion U.S. dollars -> # M Dollars
                try {
                    String nextWord = words.get(i + 1);
                    if (nextWord.equalsIgnoreCase("million") || nextWord.equalsIgnoreCase("billion")) {
                        String nextNextWord = words.get(i + 2);
                        if (nextNextWord.equalsIgnoreCase("U.S.")) {
                            String nextNextNextWord = words.get(i + 3);
                            if (nextNextNextWord.equalsIgnoreCase("dollars")) {
                                if (nextWord.equalsIgnoreCase("million")) {
                                    parsedLine.add(word);
                                    parsedLine.add("M");
                                    parsedLine.add("Dollars");
                                }
                                else {
                                    Double wordToMultiply = new Double(word);
                                    wordToMultiply *= 1000;
                                    String multipliedWord = wordToMultiply.toString().substring(0,wordToMultiply.toString().length()-2);
                                    parsedLine.add(word);
                                    parsedLine.add(multipliedWord);
                                    parsedLine.add("M");
                                    parsedLine.add("Dollars");
                                }
                                i += 3;
                            }
                        }
                    }else{
                        parsedLine.add(word);
                    }
                } catch (Exception e) { // doesn't satisfy the pattern
                    parsedLine.add(word);
                }
            }
            else{ // not a price
                parsedLine.add(word);
            }
        }

        return parsedLine;
    }


    /**
     * Checks if a given string is one of the words "Thousand", "Million" or "Billion", and returns a matching character for each one.
     * In case the given string is none of these words, null is returned.
     * @param bigNumber the given string
     * @return a string that matches the magnitude of the number, or null if the conditions are not satisfied
     */
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

    /**
     * Checks if a given string is a name of a month, and returns the matching number of the month as a string.
     * In case the given string is not a month, an empty string is returned.
     * @param month the given string
     * @return the matching number of the month as a string
     */
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

    /**
     * Parses two given strings into a date format.
     * The given strings are two components of a date. The first component is either a name of a month or a number of the day in the month,
     * and the second component is either a year or a name of a month.
     * @param datePart1 the first component of the date
     * @param datePart2 the second component of the date
     * @return a parsed form of the date, composed of the two given date parts
     */
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

    /**
     * Parses the numerical string with dependency in what the word after it is.
     * @param word the numerical term
     * @param nextWord the following word
     * @return the parsed form of the number
     */
    private String parseNumber(String word, String nextWord){
        try {
            String character = convertNumberFromTextToChar(nextWord); //checks the pattern # thousand / million / billion - step one
            this.skipNextWord = true;
            if (isPercent(nextWord)) { // checks the percentage pattern
                return word + "%";
            } else if(nextWord.equals("M")){ //checks the prices over million pattern
                return word + nextWord;
            } else if (character != null) { //checks the pattern # thousand / million / billion - next two
                return word + character;
            } else if (!convertMonthToNumber(nextWord).equals("")) { //covers cases of: DD MM
                return parseDates(word, nextWord);
            }
            throw new Exception("Next word doesnt belong to the number");
        } catch (Exception e) { // if there is no extra word after "word" or nextWord doesnt belong to any special case
            this.skipNextWord = false;
            return parseNumber(word);
        }
    }


    /**
     * Parses the words in a given words collection in an article.
     * @param articleWords words collection in an article
     * @return an ArrayList that contains the parsed words in the articleWords
     */
    private ArrayList<String> parseArticleWords(ArrayList<String> articleWords) {
        ArrayList<String> parsedWords = new ArrayList<>();

        // Add hash
        int wordsNumber = articleWords.size();
        for (int i = 0; i < wordsNumber ; i++) {
            //cleans unnecessary characters from the word
            //TODO: make sure this is ok
            String word = articleWords.get(i); //.replaceAll(",", "");
            /*
            if(word.startsWith("-") || word.endsWith("-")){
                word = word.replace("-", "");
            }
            if(word.startsWith("/")){
                while(!word.equals("") && word.charAt(0) == '/')
                    word = word.substring(1);
            }
            if(word.endsWith("/")){
                while(!word.equals("") && word.charAt(word.length()-1) == '/')
                    word = word.substring(0,word.length()-1);
            }
            if(word.contains(",")){
                word = word.replace(",","");
            }
            if(word.equals("")){
                continue;
            }*/
            String nextWord = i + 1 < wordsNumber ? articleWords.get(i + 1).replaceAll(",", ""): "";
            if (word.length() > 1) {
                if (isNumber(word)) {
                    parsedWords.add(parseNumber(word, nextWord));
                }
                else if (!convertMonthToNumber(word).equals("")){ //checks the date formats
                    try{
                        if(isNumber(nextWord)){
                            String newWord = parseDates(word, nextWord);
                            if(newWord != null) {
                                this.skipNextWord = true;
                                parsedWords.add(newWord);
                            }else{
                                parsedWords.add(word);
                                this.skipNextWord = false;
                            }
                        }
                        else{
                            parsedWords.add(word);
                            this.skipNextWord = false;

                        }
                    }
                    catch (Exception e){
                        parsedWords.add(word);
                        this.skipNextWord = false;
                    }
                }
                else if(word.equalsIgnoreCase("between")){
                    try{
                        String doubleNextWord = articleWords.get(i+2);
                        if (isNumber(nextWord) && isNumber(doubleNextWord)){ //if the next two words after "between" are actually numbers (ignoring stop words)
                            parsedWords.add(nextWord + "-" + doubleNextWord);
                            parsedWords.add(parseNumber(nextWord));
                            parsedWords.add(parseNumber(doubleNextWord));
                            i+=1;
                            this.skipNextWord = true;
                        }
                    }catch(Exception e){
                        parsedWords.add(word);
                    }
                } //parses kilo-metrics
                else if(word.toLowerCase().startsWith("kilo") && word.length() > 4){
                    switch (word.substring(4).toLowerCase()){
                        case "meter":
                            parsedWords.add("km");
                            break;

                        case "meters":
                            parsedWords.add("km");
                            break;

                        case "gram":
                            parsedWords.add("kg");
                            break;

                        case "grams":
                            parsedWords.add("kg");
                            break;

                        case "calories":
                            parsedWords.add("kcal");
                            break;

                        case "byte":
                            parsedWords.add("KB");
                            break;

                        case "bytes":
                            parsedWords.add("KB");
                            break;

                        default:
                            parsedWords.add(word);
                            break;
                    }
                    this.skipNextWord = false;
                }
                else{ //no special parsing rule for the word
                    parsedWords.add(word);
                    this.skipNextWord = false;
                } //in case the term was parsed along with the word after it
                if(this.skipNextWord){
                    i++;
                }
            }
        }

        return parsedWords;
    }


    /**
     * Parses the content of a given document, according to the rules that are defined in this class's functions.
     * The words are being parsed and added to the returned ArrayList.
     * @param article the given document
     * @param stem an indicator of activating stemming on each term
     * @return an ArrayList of the parsed words
     */
    public HashMap<String,Term> parse(Article article, boolean stem) {
        dictionary = new HashMap<>();
        termPositionInDocument = 0;
        String content = article.getContent();
        ArrayList<String> entitiesInDoc = parseEntities(content);
        ArrayList<String> words = new ArrayList<>(Arrays.asList(content.replace("--", ", ").split(REGEX_BY_WORDS)));
        words = cleanWords(words);
       // words = mergeWordsAndEntities(words,entitiesInDoc,article.getDocId());
        words = eliminateStopWords(words);
        words = handleDollarCases(words);
        words = pricesOverMillion(words);
        ArrayList<String> parsedWords = parseArticleWords(words);
        for(String word : parsedWords){
            if(stem) {
                Stemmer.setCurrent(word);
                Stemmer.stem();
                word = Stemmer.getCurrent();
            }
            Term term;
            if(Character.isDigit(word.charAt(0))){
                if (!dictionary.containsKey(word)){
                    term = new Term(word);
                    dictionary.put(word,term);
                }
                else{
                    term = dictionary.get(word);
                }
            }/*else if(Character.isUpperCase(word.charAt(0)) && word.contains(" ")){ //in case the term is an entity
                if(!dictionary.containsKey(word)){
                    term = new Term(word);
                    term.setEntity();
                    dictionary.put(word,term);
                }
                else{
                    term = dictionary.get(word);
                }
                term.addPositionInDoc(article,termPositionInDocument);
                continue;
            }*/
            else{
                //checks if the dictionary contains this word and how the word should be indexed
                if (!dictionary.containsKey(word.toLowerCase())) {
                    if (Character.isUpperCase(word.charAt(0)))
                        term = new Term(word.toUpperCase());
                    else
                        term = new Term(word);
                    dictionary.put(word.toLowerCase(), term);
                } else {
                    term = removeDuplicatesTermsParser(word);
                }
            }
            term.addPositionInDoc(article,termPositionInDocument);
            termPositionInDocument++;
        }

        for(String entity : entitiesInDoc){
            if(stem) {
                Stemmer.setCurrent(entity);
                Stemmer.stem();
                entity = Stemmer.getCurrent();
            }
            Term term;
            if(!dictionary.containsKey(entity)){
                term = new Term(entity);
                term.setEntity();
                dictionary.put(entity,term);
            }
            else{
                term = dictionary.get(entity);
            }
            term.addPositionInDoc(article,0);
        }

        return dictionary;
    }

    /**
     * Gets a word from the parse function and updates the term's name if needed
     * @param word the given word as appeared in the doc
     * @return the updated object of the term
     */
    private Term removeDuplicatesTermsParser(String word){
        if(Character.isDigit(word.charAt(0))){
            return dictionary.get(word);
        }
        Term termInDic = dictionary.get(word.toLowerCase());
        if(Character.isLowerCase(word.charAt(0))) {
            termInDic.setTerm(word);
        }
        return termInDic;
    }

// ----------------------------------------------------------------PART B ADDITIONS-----------------------------------------------------------------------------------

    /**
     * Parses a query, and stems it if required.
     * @param query the list of the words in the query
     * @param stem an indicator of whether to stem the parsed words or not
     * @return a list of the words in the query after parsing (and stemming)
     */
    public ArrayList<String> parseQuery(ArrayList<String> query, boolean stem){
        ArrayList<String> parsedQuery = new ArrayList<>(query);
        parsedQuery = eliminateStopWords(parsedQuery);
        parsedQuery = handleDollarCases(parsedQuery);
        parsedQuery = pricesOverMillion(parsedQuery);
        parsedQuery = parseArticleWords(parsedQuery);

        if(stem){
            ArrayList<String> parsedQueryWithStemming = new ArrayList<>();
            for(String word : parsedQuery){
                Stemmer.setCurrent(word);
                Stemmer.stem();
                parsedQueryWithStemming.add(Stemmer.getCurrent());
            }
            return parsedQueryWithStemming;
        }
        return parsedQuery;
    }


/*
    private ArrayList<String> mergeWordsAndEntities(ArrayList<String> words, ArrayList<String> entities, String docID) {
        ArrayList<String> mergedWordsAndEntities = new ArrayList<>();
        String[] splitEntity;
        int i=0;
        boolean isEntity;
        for(String entity: entities){
            splitEntity = entity.split(" ");
            int entitySize = splitEntity.length;
            while(i<words.size()){
                String word = words.get(i);
                if (word.length() > 0 && Character.isUpperCase(word.charAt(0))){
                    isEntity = true;

                    try {
                        for (int j = 0; j < entitySize; j++) {
                            if (!Character.isUpperCase(words.get(i + j).charAt(0)) || !words.get(i + j).equalsIgnoreCase(splitEntity[j])) {
                                isEntity = false;
                                for (int k = 0; k <= j; k++) {
                                    mergedWordsAndEntities.add(words.get(i + k));
                                }
                                i += j + 1;
                                break;
                            }
                        }
                    }
                    catch(IndexOutOfBoundsException e){
                        System.out.println(docID);
                    }
                    if(isEntity){
                        mergedWordsAndEntities.add(entity);
                        for(int j=0; j<entitySize;j++){
                            mergedWordsAndEntities.add(words.get(i));
                            i++;
                        }
                        break;
                    }

                }
                else{
                    mergedWordsAndEntities.add(word);
                    i++;
                }
            }
        }
        while(i<words.size()){
            mergedWordsAndEntities.add(words.get(i));
            i++;
        }
        return mergedWordsAndEntities;
    }
*/

    private ArrayList<String> parseEntities(String content){
        ArrayList<String> entitiesInDoc = new ArrayList<>();
        Pattern entities = Pattern.compile("(?:\\w{0,}[A-Z]+\\w*(?:-[A-Za-z]+)*(?:\\W|\\s+)){2,4}",Pattern.MULTILINE);
        Matcher foundEntities = entities.matcher(content);

        while (foundEntities.find()) {
            String entity = foundEntities.group();
            entity = Pattern.compile("[:-]").matcher(entity).replaceAll(" ");
            entity = Pattern.compile("[,.;)?!}\\]\"'*|]").matcher(entity).replaceAll("");
            entity = Pattern.compile("\n|\\s+").matcher(entity).replaceAll(" ").trim();
            entity = entity.replaceAll("^(?:\\w\\s)+", "");
            entity = entity.toUpperCase();
            entitiesInDoc.add(entity);
        }
        return entitiesInDoc;
    }


    private ArrayList<String> cleanWords(ArrayList<String> words){
        ArrayList<String> results = new ArrayList<>();
        for(String word : words) {
            word = word.replaceAll(",", "");
            if (word.startsWith("-") || word.endsWith("-")) {
                word = word.replace("-", "");
            }
            if (word.startsWith("/")) {
                while (!word.equals("") && word.charAt(0) == '/')
                    word = word.substring(1);
            }
            if (word.endsWith("/")) {
                while (!word.equals("") && word.charAt(word.length() - 1) == '/')
                    word = word.substring(0, word.length() - 1);
            }
            if (word.contains(",")) {
                word = word.replace(",", "");
            }
            if (word.equals("")) {
                continue;
            }
            results.add(word);
        }
        return results;
    }


}
