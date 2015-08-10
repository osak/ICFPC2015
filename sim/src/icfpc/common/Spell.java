package icfpc.common;

/**
 * @author masata
 */
public enum Spell {
    EI("ei!"),
    IAIA("ia! ia!"),
    RLYEH("r’lyeh"),
    YUGGOTH("yuggoth"),
    YOGSOTHOTH("yogsothoth"),
    NECRONOMICON("necronomicon"),
    SENTENCE51("In his house at R'lyeh dead Cthulhu waits dreaming.");

    public final String phrase;

    Spell(final String phrase) {
        this.phrase = phrase;
    }
}
