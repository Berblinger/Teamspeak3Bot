package de.backxtar.gw2;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.backxtar.Config;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CallGuild {

    public static class GWCallGuild {
        public long level;
        public String motd;
        public long influence;
        public long aetherium;
        public long resonance;
        public long favor;
        public int member_count;
        public int member_capacity;
        public String id;
        public String name;
        public String tag;
    }

    public static GWCallGuild getGuild(String guildID) {
        String json = "";
        try {
            json = Gw2Utils.getJson("https://api.guildwars2.com/v2/guild/" + guildID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        return gson.fromJson(json, GWCallGuild.class);
    }

    public static GWCallGuild getOwnGuild() {
        String json = "";
        try {
            json = Gw2Utils.getJson("https://api.guildwars2.com/v2/guild/" + Config.getConfigData().guildID +
                    "?access_token=" + Config.getConfigData().guildLeaderApiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        return gson.fromJson(json, GWCallGuild.class);
    }

    public static class GWCallGuildMembers {
        public String name;
        public String rank;
    }

    public static List<GWCallGuildMembers> getMembers() {
        String json = "";
        try {
            json = Gw2Utils.getJson("https://api.guildwars2.com/v2/guild/" + Config.getConfigData().guildID +
                    "/members?access_token=" + Config.getConfigData().guildLeaderApiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        Type callGuildMembers = new TypeToken<ArrayList<GWCallGuildMembers>>(){}.getType();
        return gson.<ArrayList<GWCallGuildMembers>>fromJson(json, callGuildMembers);
    }
}
