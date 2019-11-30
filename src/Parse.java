import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Parse {

    private boolean isFraction(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+/(\\d+)?");
        return pattern.matcher(strNum).matches();
    }

    private boolean isNumber(String strNum) {
        if (strNum == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        return pattern.matcher(strNum).matches() || isFraction(strNum);
    }

    float    parseFraction(String ratio) {
        if (ratio.contains("/")) {
            String[] rat = ratio.split("/");
            return Float.parseFloat(rat[0]) / Float.parseFloat(rat[1]);
        } else {
            return Float.parseFloat(ratio);
        }
    }

    private String parseNumber(String number){
        DecimalFormat df = new DecimalFormat("#.###");
        float parsedNumber = parseFraction(number);
        if(parsedNumber >= 1000){
            if(parsedNumber >= 1000000){
                if(parsedNumber >= 1000000000){
                    return df.format(parsedNumber / 1000000000) +"B";
                }
                return df.format(parsedNumber / 1000000)+"M";
            }
            return df.format(parsedNumber / 1000)+"K";
        }
        return number;
    }

    private boolean isPercent(String precentage){
        return "percent".equals(precentage) || "percentage".equals(precentage);
    }

    private ArrayList<String> parseLine(String line){
        ArrayList<String> parsedWords = new ArrayList<>();
        ArrayList<String> words = new ArrayList<>(Arrays.asList(line.split("[^\\w/]+")));
        for(int i = 0; i < words.size(); i++){
            String word = words.get(i).replaceAll(",", "");
            if(isNumber(word)){
                try{
                    String nextWord = words.get(i+1).replaceAll(",", "");
                    if(isPercent(nextWord)){
                        parsedWords.add(word+"%");
                        i++;
                    }
                    else if("Thousand".equals(nextWord) || "Thousand".toLowerCase().equals(nextWord) || "Thousand".toUpperCase().equals(nextWord)){
                        parsedWords.add(word+"K");
                        i++;
                    }
                    else if("Million".equals(nextWord) || "Million".toLowerCase().equals(nextWord) || "Million".toUpperCase().equals(nextWord)){
                        parsedWords.add(word+"M");
                        i++;
                    }
                    else if("Billion".equals(nextWord) || "Billion".toLowerCase().equals(nextWord) || "Billion".toUpperCase().equals(nextWord)){
                        parsedWords.add(word+"B");
                        i++;
                    }
                }catch (Exception e){
                    parsedWords.add(parseNumber(word));
                }
            }
        }
        return parsedWords;
    }

    public ArrayList<String> parse(Article article){
        ArrayList<String> articleLines = new ArrayList<>(Arrays.asList(article.getContent().split("\\r?\\n")));
        ArrayList<String> parsedWords = new ArrayList<>();
        for(String line: articleLines){
            parsedWords.addAll(parseLine(line));
        }
        return parsedWords;
    }
}
