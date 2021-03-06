package de.mrcloud.listeners.statistiks;

import de.mrcloud.SQL.SqlMain;
import de.mrcloud.utils.DataStorageClass;
import de.mrcloud.utils.JDAUtils;
import de.mrcloud.utils.WrappedInvite;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ActivityListener extends ListenerAdapter {

    public static HashMap<String, String> timeInChannel = new HashMap<>();


    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent e) {
        super.onGuildVoiceJoin(e);


        //Varibles
        Guild server = e.getGuild();
        VoiceChannel voiceChannelJoined = e.getChannelJoined();
        Category category = e.getChannelJoined().getParent();
        Member member = e.getMember();
        //-----------

        //Starts the time counting when you join a channel
        if (!e.getChannelJoined().getId().equals("514517861440421907")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM);
            ZonedDateTime hereAndNow = ZonedDateTime.now();
            String test = dateTimeFormatter.format(hereAndNow);
            String date = test.replaceAll(",", "");
            timeInChannel.put(member.getUser().getId(), date);
        }

    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent e) {
        super.onGuildVoiceMove(e);

        //variables
        Guild server = e.getGuild();
        VoiceChannel voiceChannelJoined = e.getChannelJoined();
        VoiceChannel voiceChannelLeft = e.getChannelLeft();
        Category category = e.getChannelJoined().getParent();
        Member member = e.getMember();

        //Saves your channel time when you join a afk channel by calling the saveChannelTime Method
        if (e.getChannelJoined().getId().equals("514517861440421907")) {
            saveChannelTime(member, timeInChannel);
        }
        if (e.getChannelLeft().getId().equals("514517861440421907")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM);
            ZonedDateTime hereAndNow = ZonedDateTime.now();
            String test = dateTimeFormatter.format(hereAndNow);
            String date = test.replaceAll(",", "");
            timeInChannel.put(member.getUser().getId(), date);
        }
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent e) {
        super.onGuildVoiceLeave(e);


        //Variables
        Guild server = e.getGuild();
        VoiceChannel voiceChannelJoined = e.getChannelJoined();
        VoiceChannel voiceChannelLeft = e.getChannelLeft();
        Member member = e.getMember();

        //Saves your channel time when you leave a voice channel by calling the saveChannelTime Method
        saveChannelTime(member, timeInChannel);

    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent e) {
        super.onGuildMessageReceived(e);
        Message message = e.getMessage();
        String messageContent = message.getContentRaw();
        TextChannel txtChannel = e.getChannel();


        JDAUtils utils = new JDAUtils();

        if(!message.isWebhookMessage() && !e.getMember().getUser().isBot() && !txtChannel.getId().equals("514517861440421907")) {
            Guild server = e.getGuild();
            Member member = Objects.requireNonNull(e.getMember());


            int messagesBefore = utils.getSqlCollumInt(SqlMain.mariaDB(),"MessageCount",member);
            utils.setSQLCollum(SqlMain.mariaDB(),member.getId(),"MessageCount",Integer.toString((messagesBefore + 1)));
        }
    }



    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent e) {
        super.onGuildMemberJoin(e);

        Guild server = e.getGuild();
        Member member = e.getMember();
        List<WrappedInvite> invites = DataStorageClass.invitesList;

         server.retrieveInvites().queue(invitesToCompare -> {
           for(Invite newInvite : invitesToCompare)  {
               for(WrappedInvite oldInvite: invites) {
                   if(newInvite.getCode().equals(oldInvite.getInviteCode())) {
                       if(newInvite.getUses() > oldInvite.getUses()) {
                           int oldCountOfInvites = JDAUtils.getSqlCollumInt(SqlMain.mariaDB(),"invites",member);
                           server.getTextChannelById(617057983783895045L).sendMessage(newInvite.getInviter().getAsMention() + " hat " + member.getAsMention() + " eingeladen. (**" + oldCountOfInvites + "** invites)").queue();
                           JDAUtils.setSQLCollumInt(SqlMain.mariaDB(),member.getId(),"invites",oldCountOfInvites);
                       }
                   }
               }
           }
         });
    }

    //The save channel time method
    public void saveChannelTime(Member member, HashMap<String, String> timeInChannel) {
        Statement statement = null;


        {
            try {
                statement = SqlMain.mariaDB().createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //Variables for calculation
        long diff2 = 0L;


        long seconds = 0L;
        long min = 0L;
        long hour = 0L;
        long day = 0L;


        long diffDays = 0L;
        long diffHours = 0L;
        long diffMinutes = 0L;
        long diffSeconds = 0L;
        //---------------------

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


        String date = timeInChannel.get(member.getUser().getId());

        //Formats current time in a normal format
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM);
        ZonedDateTime hereAndNow = ZonedDateTime.now();
        String test = dateTimeFormatter.format(hereAndNow);
        String stopDate = test.replaceAll(",", "");
        //---------------------------------------


        //SQL Part
        try {
            //Gets all lines in the sql table which have the user ID as a key
            ResultSet resultSetToSet = statement.executeQuery("SELECT * FROM Users WHERE UserID = " + member.getUser().getId() + ";");

            while (resultSetToSet.next()) {
                //Gets the retrieved info and writes it to variables
                day = resultSetToSet.getLong("channelTimeDays");
                hour = resultSetToSet.getLong("channelTimeHours");
                min = resultSetToSet.getLong("channelTimeMinutes");
                seconds = resultSetToSet.getLong("channelTimeSeconds");
            }

            //Turns this into milliseconds for easier calculations
            seconds = seconds * 1000;
            min = min * 60 * 1000;
            hour = hour * 60 * 60 * 1000;
            day = day * 60 * 60 * 1000 * 24;

            //Adds up all the miliseconds
            diff2 = seconds + min + hour + day;
        } catch (SQLException e2) {
            e2.printStackTrace();
        }


        Date d1 = null;
        Date d2 = null;
        try {
            //Formats the channel join and leave time
            d1 = format.parse(date);
            d2 = format.parse(stopDate);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        //Calculates the minutes and seconds
        long diff = d2.getTime() - d1.getTime();
        diff += diff2;
        diffSeconds = diff / 1000 % 60;
        diffMinutes = diff / (60 * 1000) % 60;
        diffHours = diff / (60 * 60 * 1000) % 24;
        diffDays = diff / (60 * 60 * 1000 * 24);

        try {

            ResultSet resultSetCheck = statement.executeQuery("SELECT * FROM Users WHERE UserID = " + member.getUser().getId() + ";");

            if (resultSetCheck != null && resultSetCheck.next()) {
                //Writes these to the database
                statement.executeQuery("UPDATE Users SET channelTimeDays = " + diffDays + " WHERE UserID = " + member.getId() + ";");
                statement.executeQuery("UPDATE Users SET channelTimeHours = " + diffHours + " WHERE UserID = " + member.getId() + ";");
                statement.executeQuery("UPDATE Users SET channelTimeMinutes = " + diffMinutes + " WHERE UserID = " + member.getId() + ";");
                statement.executeQuery("UPDATE Users SET channelTimeSeconds = " + diffSeconds + " WHERE UserID = " + member.getId() + ";");
            }
        } catch (SQLException e1) {
            e1.printStackTrace();


        }
        //Catches exeptions and closes the Database connection
        finally {
            try {
                SqlMain.mariaDB().close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        timeInChannel.remove(member.getUser().getId());

    }
}
