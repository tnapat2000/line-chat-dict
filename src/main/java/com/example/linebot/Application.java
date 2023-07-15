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
        
        String responseMessage = "This word does not have meanings";
        JSONParser jParser = new JSONParser();
        JSONObject jObject;
        try {
            jObject = (JSONObject) jParser.parse(content.toString());
            return new TextMessage(content.toString());

        } catch (ParseException e1) {
            e1.printStackTrace();
            return new TextMessage("Error");
        }
        // return new TextMessage(jObject.toString());

        // JSONObject data = jObject.getJSONObject("meanings");
        // String results = data.getString("word");

        // return new TextMessage(data.toString());
        // return new TextMessage(content.toString());
    }
}
