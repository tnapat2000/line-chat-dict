package com.example.linebot;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
public class Application {
    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

    @EventMapping
    public Message handleTextMessage(MessageEvent<TextMessageContent> e) {
        System.out.println("event: " + e);
        TextMessageContent message = e.getMessage();
        String urlString = "https://api.dictionaryapi.dev/api/v2/entries/en/" + message.getText().toString();
        StringBuilder content= new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() != 200){
                return new TextMessage("This word has no meaning");
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            con.disconnect();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        
        String defaultResponseMessage = "This word does not have meanings";
        JSONParser jParser = new JSONParser();
        JSONArray jArray;
        // return new TextMessage(content.toString());
        StringBuilder stringBuilder = new StringBuilder("\n");
        try {
            jArray = (JSONArray) jParser.parse(content.toString());

            for(int i = 0 ; i < jArray.size(); i++) {
                JSONObject jObject= (JSONObject) jArray.get(i);                
                JSONArray meaningsJson = (JSONArray) jObject.get("meanings");
                // System.out.println(meaningsJson);
                // System.out.println(meaningsJson.size());
                for (int j = 0; j < meaningsJson.size(); j ++){
                    // System.out.println(meaningsJson.get(j));
                    JSONObject nounJsonObject = (JSONObject) meaningsJson.get(j);

                    // part of speech
                    String partOfSpeechInput = nounJsonObject.get("partOfSpeech").toString();
                    String partOfSpeech = partOfSpeechInput.substring(0, 1).toUpperCase() + partOfSpeechInput.substring(1);
                    stringBuilder.append("> " + partOfSpeech + ": ");

                    // definitions
                    JSONArray defJsonArray = (JSONArray) nounJsonObject.get("definitions");
                    stringBuilder.append(((JSONObject)defJsonArray.get(0)).get("definition"));
                    stringBuilder.append("\n");

                    // synonyms
                    JSONArray synonymArray = (JSONArray)nounJsonObject.get("synonyms");
                    if (synonymArray.size() > 0) {
                        stringBuilder.append("> " + "Synonyms: " + "\n");
                        for (int k = 0; k < synonymArray.size(); k++){
                            stringBuilder.append(" |> "+synonymArray.get(k) + "\n");
                        }
                    }
                    stringBuilder.append("\n");
                }
                if (i == jArray.size() -1) {stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length()-1);}
            }
            if (stringBuilder.length() <= 0) {return new TextMessage(defaultResponseMessage);}
            return new TextMessage(stringBuilder.toString());

        } catch (ParseException e1) {
            e1.printStackTrace();
            return new TextMessage(defaultResponseMessage);
        }
    }
}
