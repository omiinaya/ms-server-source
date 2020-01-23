package net.swordie.ms.client.character.commands;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.connection.packet.Effect;
import net.swordie.ms.connection.packet.UserPacket;
import net.swordie.ms.enums.AccountType;
import net.swordie.ms.enums.BaseStat;
import net.swordie.ms.scripts.ScriptManagerImpl;
import net.swordie.ms.scripts.ScriptType;
import net.swordie.ms.util.Util;
import net.swordie.ms.world.event.InGameEventManager;
import org.apache.log4j.LogManager;
import net.swordie.ms.connection.packet.*;
import net.swordie.ms.constants.JobConstants.JobEnum;
import net.swordie.ms.enums.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.*;

import static net.swordie.ms.enums.ChatType.Mob;
import static net.swordie.ms.enums.ChatType.*;

public class PlayerCommands {
    static final org.apache.log4j.Logger log = LogManager.getRootLogger();

    @Command(names = {"check", "dispose", "fix"}, requiredType = AccountType.Player)
    public static class Dispose extends PlayerCommand {
        public static void execute(Char chr, String[] args){
            chr.dispose();
            Map<BaseStat, Integer> basicStats = chr.getTotalBasicStats();
            StringBuilder sb = new StringBuilder();
            List<BaseStat> sortedList = Arrays.stream(BaseStat.values()).sorted(Comparator.comparing(Enum::toString)).collect(Collectors.toList());
            for (BaseStat bs : sortedList) {
                sb.append(String.format("%s = %d, ", bs, basicStats.getOrDefault(bs, 0)));
            }
            chr.chatMessage(Mob, String.format("X=%d, Y=%d, Stats: %s", chr.getPosition().getX(), chr.getPosition().getY(), sb));
            ScriptManagerImpl smi = chr.getScriptManager();
            // all but field
            smi.stop(ScriptType.Portal);
            smi.stop(ScriptType.Npc);
            smi.stop(ScriptType.Reactor);
            smi.stop(ScriptType.Quest);
            smi.stop(ScriptType.Item);
        }
    }

    @Command(names = {"event"}, requiredType = AccountType.Player)
    public static class JoinEvent extends PlayerCommand {
        public static void execute(Char chr, String[] args){
            InGameEventManager.getInstance().joinPublicEvent(chr);
        }
    }

    @Command(names = {"roll"}, requiredType = AccountType.Player)
    public static class OneArmedBandit extends PlayerCommand {
        public static void execute(Char chr, String[] args){

            String[] str = new String[] {
                    "Map/Effect.img/miro/frame",
                    "Map/Effect.img/miro/RR1/" + Util.getRandom(4),
                    "Map/Effect.img/miro/RR2/" + Util.getRandom(4),
                    "Map/Effect.img/miro/RR3/" + Util.getRandom(4)
            };

            for (String s : str) {
                chr.write(UserPacket.effect(Effect.effectFromWZ(s, false, 0, 4, 0)));
            }
        }
    }

    @Command(names = {"rebirth", "rb"}, requiredType = AccountType.Player)
    public static class Rebirth extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            short id = 0;
            JobEnum job = JobEnum.getJobById(id);
            short level = chr.getLevel();
            if (level == 250) {
                chr.setStatAndSendPacket(Stat.level, 1);
                chr.setStatAndSendPacket(Stat.exp, 0);
                chr.setJob(id);
                Map<Stat, Object> stats = new HashMap<>();
                stats.put(Stat.subJob, id);
                chr.getClient().write(WvsContext.statChanged(stats));
            }
            else {
                chr.chatMessage(Notice2, "You are not Lv.250");
                return;
            }
        }
    }
}
