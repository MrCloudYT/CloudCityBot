package de.mrcloud.listeners;

import de.mrcloud.SQL.SqlMain;
import de.mrcloud.listeners.csgo.AutoCreateChannels;
import de.mrcloud.utils.JDAUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SearchingForMatchmakingListener extends ListenerAdapter {
    //A map to compare a rank name to his rank number
    public static LinkedHashMap<String, Integer> compare = new LinkedHashMap<>();
    //Same in reverse
    public static LinkedHashMap<Integer, String> compareReverse = new LinkedHashMap<>();
    //Compares
    public static LinkedHashMap<String, String> compareEmojiToRole = new LinkedHashMap<>();
    //A list of persons which are searching for a mm
    static List<Member> searchingForMatchmaking = new ArrayList<>();

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent e) {
        super.onGuildVoiceJoin(e);
        int rankNumber = 0;

        Guild server = e.getGuild();
        VoiceChannel voiceChannelJoined = e.getChannelJoined();
        Member member = e.getMember();
        JDAUtils utils = new JDAUtils();

        int i = 0;
        int i2 = 0;
        int getRole;
        int getRole2 = 0;
        double durschnittsRang;
        //Uses my method in utils to easily get a sql collum which in this case contains the FriendCode
        String friendCodeToSend = utils.getSqlCollumString(Objects.requireNonNull(SqlMain.mariaDB()), "FriendCode", member);


        //Checks if you joined a channel named searching for matchmaking
        if (voiceChannelJoined.getName().equals("Searching-For-Matchmaking")) {
            int rankNumber2 = 0;
            String roleName2;
            //Searches for the matchmaking "overrole" and then gets the underlying role and then converts it to
            while (member.getRoles().size() > getRole2) {
                roleName2 = member.getRoles().get(getRole2).getName();

                if (roleName2.equals("╚═══ Wettkampf Rang ═══╗")) {
                    rankNumber2 = compare.get(member.getRoles().get((getRole2 + 1)).getName());

                }
                getRole2++;
            }

            List<VoiceChannel> list = server.getVoiceChannelCache().applyStream(it ->
                    it.filter(channel -> channel.getName().matches("Matchmaking \\d*"))
                            .collect(Collectors.toList())
            );

            assert list != null;
            while (list.size() > i) {

                List<Member> membersInVoice = list.get(i).getMembers();


                if (!(membersInVoice.size() >= 5)) {

                    while (membersInVoice.size() > i2) {

                        getRole = 0;


                        while (membersInVoice.get(i2).getRoles().size() > getRole) {


                            String roleName = membersInVoice.get(i2).getRoles().get(getRole).getName();
                            getRole++;


                            if (roleName.equals("╚═══ Wettkampf Rang ═══╗")) {
                                rankNumber += compare.get(membersInVoice.get(i2).getRoles().get(getRole).getName());
                                getRole = 30;

                            }
                        }
                        i2++;
                        if (membersInVoice.size() == i2 || membersInVoice.size() == 1) {
                            //noinspection IntegerDivisionInFloatingPointContext
                            durschnittsRang = Math.ceil(rankNumber / membersInVoice.size());

                            if (durschnittsRang > rankNumber2 && durschnittsRang < (rankNumber2 + 3)) {
                                server.moveVoiceMember(member, list.get(i)).queue();
                                //Checks if a friend code exists in the db
                                if (!friendCodeToSend.isEmpty()) {
                                    AutoCreateChannels.channelOwner.get(list.get(i)).getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The friendcode of " + member.getNickname() + " is " + friendCodeToSend).queue());
                                }
                            } else if (durschnittsRang < rankNumber2 && durschnittsRang > (rankNumber2 - 3)) {
                                server.moveVoiceMember(member, list.get(i)).queue();
                                //Checks if a friend code exists in the db
                                if (!friendCodeToSend.isEmpty()) {
                                    AutoCreateChannels.channelOwner.get(list.get(i)).getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The friendcode of " + member.getNickname() + " is " + friendCodeToSend).queue());
                                }
                            } else if (durschnittsRang == rankNumber2) {
                                server.moveVoiceMember(member, list.get(i)).queue();
                                //Checks if a friend code exists in the db
                                if (!friendCodeToSend.isEmpty()) {
                                    AutoCreateChannels.channelOwner.get(list.get(i)).getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The friendcode of " + member.getNickname() + " is " + friendCodeToSend).queue());
                                }

                            }
                        }
                    }

                }
                i++;
            }

        } else if (voiceChannelJoined.getName().matches("matchmaking \\d*")) {
            if (!friendCodeToSend.isEmpty()) {
                AutoCreateChannels.channelOwner.get(voiceChannelJoined).getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The friendcode of " + member.getNickname() + " is " + friendCodeToSend).queue());
            }
        }
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent e) {
        super.onGuildVoiceMove(e);

        int rankNumber = 0;

        Guild server = e.getGuild();
        VoiceChannel voiceChannelJoined = e.getChannelJoined();
        Member member = e.getMember();
        JDAUtils utils = new JDAUtils();
        int i = 0;
        int i2 = 0;
        int getRole;
        int getRole2 = 0;
        double durschnittsRang;

        String friendCodeToSend = utils.getSqlCollumString(Objects.requireNonNull(SqlMain.mariaDB()), "FriendCode", member);

        int rankNumber2 = 0;
        String roleName2;
        while (member.getRoles().size() > getRole2) {
            roleName2 = member.getRoles().get(getRole2).getName();

            if (roleName2.equals("╚═══ Wettkampf Rang ═══╗")) {
                rankNumber2 = compare.get(member.getRoles().get((getRole2 + 1)).getName());

            }
            getRole2++;
        }
        if (voiceChannelJoined.getId().equals("710482079653167115")) {

            List<VoiceChannel> list = server.getVoiceChannelCache().applyStream(it ->
                    it.filter(channel -> channel.getName().matches("Matchmaking \\d*"))
                            .collect(Collectors.toList())
            );

            assert list != null;
            while (list.size() > i) {

                List<Member> membersInVoice = list.get(i).getMembers();


                if (!(membersInVoice.size() >= 5)) {

                    while (membersInVoice.size() > i2) {

                        getRole = 0;


                        while (membersInVoice.get(i2).getRoles().size() > getRole) {


                            String roleName = membersInVoice.get(i2).getRoles().get(getRole).getName();
                            getRole++;


                            if (roleName.equals("╚═══ Wettkampf Rang ═══╗")) {
                                rankNumber += compare.get(membersInVoice.get(i2).getRoles().get(getRole).getName());
                                getRole = 30;

                            }
                        }
                        i2++;
                        if (membersInVoice.size() == i2 || membersInVoice.size() == 1) {
                            //noinspection IntegerDivisionInFloatingPointContext
                            durschnittsRang = Math.ceil(rankNumber / membersInVoice.size());

                            if (durschnittsRang > rankNumber2 && durschnittsRang < (rankNumber2 + 3)) {
                                server.moveVoiceMember(member, list.get(i)).queue();
                                //Checks if a friend code exists in the db
                                if (!friendCodeToSend.isEmpty()) {
                                    AutoCreateChannels.channelOwner.get(list.get(i)).getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The friendcode of " + member.getNickname() + " is " + friendCodeToSend).queue());
                                }
                            } else if (durschnittsRang < rankNumber2 && durschnittsRang > (rankNumber2 - 3)) {
                                server.moveVoiceMember(member, list.get(i)).queue();
                                //Checks if a friend code exists in the db
                                if (!friendCodeToSend.isEmpty()) {
                                    AutoCreateChannels.channelOwner.get(list.get(i)).getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The friendcode of " + member.getNickname() + " is " + friendCodeToSend).queue());
                                }
                            } else if (durschnittsRang == rankNumber2) {
                                server.moveVoiceMember(member, list.get(i)).queue();
                                //Checks if a friend code exists in the db
                                if (!friendCodeToSend.isEmpty()) {
                                    AutoCreateChannels.channelOwner.get(list.get(i)).getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The friendcode of " + member.getNickname() + " is " + friendCodeToSend).queue());
                                }
                            }
                        }
                    }

                }
                i++;
            }

        } else if (voiceChannelJoined.getName().matches("Matchmaking \\d*")) {
            if (!friendCodeToSend.isEmpty()) {
                if (AutoCreateChannels.channelOwner.containsKey(voiceChannelJoined)) {
                    AutoCreateChannels.channelOwner.get(voiceChannelJoined).getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The friendcode of " + member.getUser().getName() + " is " + friendCodeToSend).queue());
                }
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent e) {
        super.onGuildVoiceLeave(e);
    }
}



