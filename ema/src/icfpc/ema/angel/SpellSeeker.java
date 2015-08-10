package icfpc.ema.angel;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import icfpc.ema.Main;
import icfpc.ema.cli.CommandLineOption;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * @author tomoyuki
 */

public class SpellSeeker {
    public SpellSeeker() {}

    public Set<String> seek(int n, final String s) {
        Set<String> ret = Sets.newHashSet();
        Map<String, Integer> cnt = Maps.newHashMap();
        for (int i = 0; i < s.length() - n; i++) {
            String key = s.substring(i, i + n);
            if (!cnt.containsKey(key)) {
                cnt.put(key, 1);
            } else {
                cnt.put(key, cnt.get(key) + 1);
            }
        }

        for (String key : cnt.keySet()) {
            if (cnt.get(key) == 1) {
                ret.add(key);
            }
        }
        return ret;
    }
    public static void main(final String[] args) throws Exception {
        Main.setLogger();
        final CommandLineOption opts;
        try {
            opts = CommandLineOption.parse(args);
        } catch (final Throwable t) {
            CommandLineOption.printHelp();
            return;
        }

        if (opts.isDebugMode()) {
            Logger.getRootLogger().setLevel(Level.DEBUG);
        } else {
            Logger.getRootLogger().setLevel(Level.INFO);
        }

        final Scanner scanner = new Scanner(System.in);

        SpellSeeker spellSeeker = new SpellSeeker();
        Set<String> candidates = null;
        while (scanner.hasNext()) {
            String sentence = scanner.nextLine();
            Set<String> ret = spellSeeker.seek(opts.getSpellLength(), sentence);
            if (candidates == null) {
                candidates = ret;
            } else {
                candidates.retainAll(ret);
            }
        }

        if (candidates == null || candidates.size() == 0) {
            Logger.getRootLogger().info("Candidates of spell have not found.");
        } else {
            for (String candidate : candidates) {
                Logger.getRootLogger().info(candidate);
            }
        }
    }
}
