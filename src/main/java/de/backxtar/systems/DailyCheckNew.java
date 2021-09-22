package de.backxtar.systems;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import de.backxtar.Config;
import de.backxtar.DerGeraet;
import de.backxtar.gw2.CallDaily;
import de.backxtar.gw2.CallPactSupply;
import de.backxtar.gw2.Gw2Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DailyCheckNew {
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private static final TS3Api api = DerGeraet.getInstance().api;
    private static final Config.Colors colors = Config.getColors();

    public static void checkDailies() {
        if (Config.getConfigData().dailiesChannelID == 0) return;
        CallDaily.GWCallDaily daily = CallDaily.getDailies(false);
        CallDaily.GWCallDaily dailyTomorrow = CallDaily.getDailies(true);

        Future<StringBuilder> dailies = executor.submit(() -> getDailies(daily, false));
        Future<StringBuilder> dailiesTomorrow = executor.submit(() -> getDailies(dailyTomorrow, true));

        try {
            String des = "[center][size=11][URL=client://" + api.whoAmI().getId() + "/"
                    + api.whoAmI().getUniqueIdentifier() + "]Message me![/URL][/size]" +
                    "\n\n" + dailies.get().toString() + "\n\n\n" + dailiesTomorrow.get().toString();
            if (api.getChannelInfo(Config.getConfigData().dailiesChannelID).getDescription().equalsIgnoreCase(des))
                return;
            api.editChannel(Config.getConfigData().dailiesChannelID, ChannelProperty.CHANNEL_DESCRIPTION, des);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static StringBuilder getDailies(CallDaily.GWCallDaily dailies, boolean tomorrow) {
        StringBuilder builder = new StringBuilder();
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CallDaily.GWCallDailyNames> daileNames;
        CallDaily.GWCallDailyStrikes strikes = !tomorrow ? CallDaily.getStrikes().get(0) : CallDaily.getStrikes().get(1);
        ArrayList<CallPactSupply.GWCallPactSupply> supply = !tomorrow ? CallPactSupply.getSupplies(0) : CallPactSupply.getSupplies(1);
        //dailies.pve.size(), dailies.pvp.size(), dailies.wvw.size(), dailies.fractals.size();

        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    for (int j = 0; j < dailies.pve.size(); j++)
                        ids.add(dailies.pve.get(i).id);
                    break;
                case 1:
                    for (int j = 0; j < dailies.pvp.size(); j++)
                        ids.add(dailies.pvp.get(i).id);
                    break;
                case 2:
                    for (int j = 0; j < dailies.wvw.size(); j++)
                        ids.add(dailies.wvw.get(i).id);
                    break;
                case 3:
                    for (int j = 0; j < dailies.fractals.size(); j++)
                        ids.add(dailies.fractals.get(i).id);
                    break;
            }
        }
        daileNames = CallDaily.getDailiesName(ids);

        //1=pve, pvp, wvw, 2=dailyFractals, 3=recFractals
        List<String> dailyPvePvpWvw = splitDailies(daileNames, 1);
        List<String> dailyFractals = splitDailies(daileNames, 2);
        List<String> recFractals = splitDailies(daileNames, 3);

        builder.append("[size=11][color=" + colors.mainColor + "][b]")
                .append(!tomorrow ? "Dailies von heute:" : "Dailies von morgen:")
                .append("[/b][/color][/size]\n\n");

        builder.append("[size=10][color=" + colors.secondColor + "][b]PvE, PvP, WvW Dailies:\n")
                .append("[/b][/color][/size][size=9]");
        for (int i = 0; i < dailyPvePvpWvw.size(); i++) {
            builder.append("[size=9]").append(dailyPvePvpWvw.get(i)).append("[/size]");
            if (i < (dailyPvePvpWvw.size() - 1)) builder.append("\n");
        }
        builder.append("\n\n");

        builder.append("[size=10][color=" + colors.secondColor + "][b]Fraktal Dailies:\n")
                .append("[/b][/color][/size][size=9]");
        for (int i = 0; i < dailyFractals.size(); i++) {
            builder.append("[size=9]").append(dailyFractals.get(i)).append("[/size]");
            if (i < (dailyFractals.size() - 1)) builder.append("\n");
        }
        builder.append("\n\n");

        builder.append("[size=10][color=" + colors.secondColor + "][b]Fraktal Dailies:\n")
                .append("[/b][/color][/size][size=9]");
        for (int i = 0; i < recFractals.size(); i++) {
            builder.append("[size=9]").append(recFractals.get(i)).append("[/size]");
            if (i < (recFractals.size() - 1)) builder.append("\n");
        }
        builder.append("\n\n");

        builder.append("[size=10][color=" + colors.secondColor + "][b]Daily Strike Mission:\n")
                .append("[/b][/color][/size][img]https://i.epvpimg.com/cfb6fab.png[/img] [size=9]")
                .append(Gw2Utils.formatDailyStrike(strikes.strike));
        builder.append("[/size]\n\n");

        builder.append("[size=10][color=" + colors.secondColor + "][b]Daily Pakt-Vorratsnetz-Agenten:\n")
                .append("[/b][/color][/size] [size=9]").append(getLocations(supply));
        builder.append("[/size]\n\n");

        builder.append("[size=10][color=" + colors.secondColor + "][b]Profit:\n")
                .append("[/b][/color][/size] [size=9]")
                .append("[URL=https://wiki.guildwars2.com/wiki/Map_bonus_reward/profit]Map Belohnungen[/URL][/size]");
        return builder;
    }

    private static String getLocations(ArrayList<CallPactSupply.GWCallPactSupply> supplies) {
        StringBuilder supply = new StringBuilder();

        for (int i = 0; i < supplies.size(); i++) {
            supply.append(supplies.get(i).name).append(" @").append(supplies.get(i).location[1]);
            if (i < (supplies.size() - 1)) supply.append(", ");
        }
        return supply.toString();
    }

    private static List<String> splitDailies(ArrayList<CallDaily.GWCallDailyNames> dailyNames, int mode) {
        List<String> dailies = new ArrayList<>();

        switch (mode) {
            case 1 :
                for (CallDaily.GWCallDailyNames dailyName : dailyNames) {
                    if (dailies.contains(dailyName.name)) continue;
                    String name;
                    if (dailyName.name.contains("PvP") || dailyName.name.contains("Top Stats")) {
                        name = Gw2Utils.formatDailiesPvpWvw(dailyName.name);
                        dailies.add("[img]https://i.epvpimg.com/MLQ3fab.png[/img] " + name);
                    } else if (dailyName.name.contains("WvW") || dailyName.name.contains("Mists Guard Killer")) {
                        name = Gw2Utils.formatDailiesPvpWvw(dailyName.name);
                        dailies.add("[img]https://i.epvpimg.com/WHXtfab.png[/img] " + name);
                    } else {
                        name = Gw2Utils.formatDaily(dailyName.name);
                        dailies.add("[img]https://i.epvpimg.com/f6E1cab.png[/img] " + name);
                    }
                }
                break;

            case 2 :
                List<String> unsorted = new ArrayList<>();
                for (CallDaily.GWCallDailyNames dailyName : dailyNames) {
                    if (unsorted.contains(dailyName.name) || !dailyName.name.contains("Tier 4")) continue;
                    String name;
                    name = Gw2Utils.formatDailyFractals(dailyName.name);
                    unsorted.add(name);
                }
                sort(unsorted);

                for (String s : unsorted) dailies.add("[img]https://i.epvpimg.com/UgDScab.png[/img] " + s);
                break;

            case 3 :
                for (CallDaily.GWCallDailyNames dailyName : dailyNames) {
                    if (dailies.contains(dailyName.name) || !dailyName.name.contains("Recommended")) continue;
                    String name = Gw2Utils.formatRecFractals(dailyName.name);
                    dailies.add("[img]https://i.epvpimg.com/UgDScab.png[/img] " + name);
                }
                break;
        }
        return dailies;
    }

    private static void sort(List<String> getRecFractals) {
        getRecFractals.sort((o1, o2) -> {
            int s1int = Integer.parseInt(o1.substring(0, o1.indexOf(" ")));
            int s2int = Integer.parseInt(o2.substring(0, o2.indexOf(" ")));
            return s1int - s2int;
        });
    }
}