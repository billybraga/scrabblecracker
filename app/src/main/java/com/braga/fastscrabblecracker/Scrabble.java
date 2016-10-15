package com.braga.fastscrabblecracker;

import com.braga.utils.StopWatch;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Scrabble {
    private String board;
    private String lang;
    private String letters;
    private StopWatch sw;

    public class Response {
        public int length;
        public int serverTime;
        public int time;
        public ArrayList<HashMap<String, String>> values;
    }

    public Scrabble(String letters, String board, String lang) {
        this.letters = letters;
        this.board = board;
        this.lang = lang;
        this.sw = new StopWatch();
        this.sw.start();
    }

    public static String getQueryUrl(String letters, String board, String lang) {
        return String.format("http://scrabble.apphb.com/Crack/v3/?letters=%s&board=%s&lang=%s", new Object[]{URLEncoder.encode(letters), URLEncoder.encode(board), URLEncoder.encode(lang)});
    }

    public String getQueryUrl() {
        return getQueryUrl(this.letters, this.board, this.lang);
    }

    public String getQuery() {
        return String.format("%s&%s&%s", new Object[]{this.letters.replace("&", "%26"), this.board.replace("&", "%26"), this.lang.replace("&", "%26")});
    }

    public String getDescription() {
        return String.format("\"%s\" -> \"%s\"", new Object[]{this.letters, this.board});
    }

    public String getLang() {
        return this.lang;
    }

    public Response parseResponse(String json) throws JSONException {
        ArrayList<HashMap<String, String>> values = new ArrayList();
        JSONObject infos = (JSONObject) new JSONTokener(json).nextValue();
        JSONArray list = (JSONArray) infos.get("results");
        for (int i = 0; i < list.length(); i++) {
            HashMap<String, String> item = new HashMap();
            item.put("Word", list.getJSONObject(i).getString("Word"));
            item.put("Value", new StringBuilder(String.valueOf(list.getJSONObject(i).getString("Value"))).append(" points").toString());
            values.add(item);
        }
        this.sw.stop();
        Response response = new Response();
        response.values = values;
        response.serverTime = Integer.parseInt(infos.getString("time"));
        response.length = list.length();
        response.time = (int) this.sw.getElapsedTime();
        return response;
    }
}
