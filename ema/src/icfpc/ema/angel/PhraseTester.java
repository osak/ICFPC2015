package icfpc.ema.angel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import icfpc.common.Board;
import icfpc.common.Cell;
import icfpc.common.GameSettings;
import icfpc.common.OriginalCell;
import icfpc.io.CommandReader;
import icfpc.io.model.Problem;
import icfpc.random.Randomizer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author tomoyuki
 */

public class PhraseTester {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    final List<URL> problemFiles = Lists.newArrayList();
    private List<TestResult> testResults = Lists.newArrayList();

    public PhraseTester(final List<URL> problemFiles) throws IOException {
        this.problemFiles.addAll(problemFiles);
    }

    public PhraseTester test(final String phrase_) throws IOException {
        for (URL problemFile : problemFiles) {
            Problem problem = MAPPER.readValue(problemFile, Problem.class);
            for (int seed : problem.sourceSeeds) {
                GameSettings gameSettings = new GameSettings(problem.width, problem.height, problem. units, problem.sourceLength, seed);
                final StringCommandReader phrase = new StringCommandReader(phrase_);
                if (spell(problem, gameSettings, seed, phrase)) {
                    testResults.add(new TestResult(problemFile, seed));
                }
            }
        }
        return this;
    }

    public List<TestResult> getResult() {
        return testResults;
    }

    private boolean spell(
            final Problem problem,
            final GameSettings gameSettings,
            final int seed,
            final StringCommandReader phrase
    ) {
        Board board = createBoard(problem, gameSettings, seed);

        while (phrase.hasNext()) {
            final char cmd = phrase.next();
            if (board.hasEnded()) {
                board.violateRule("");
                return false;
            }
            board.operate(cmd);
            if (board.memo != null) {
                return false;
            }
        }
        return true;
    }

    private Board createBoard(
            final Problem problem,
            final GameSettings gameSettings,
            final int seed
    ) {
        return new Board(
                gameSettings,
                new Randomizer(seed),
                FluentIterable.from(problem.filled).transform(new Function<OriginalCell, Cell>() {
                    @Nullable
                    @Override
                    public Cell apply(OriginalCell input) {
                        return input.toCell();
                    }
                }).toImmutableList()
        );
    }

    private class StringCommandReader implements CommandReader {
        final String commands;
        private int idx;

        public StringCommandReader(final String commands) {
            this.commands = commands;
            this.idx = 0;
        }

        @Override
        public boolean hasNext() {
            return idx < commands.length();
        }

        @Override
        public char next() {
            return commands.charAt(idx++);
        }
    }
}
