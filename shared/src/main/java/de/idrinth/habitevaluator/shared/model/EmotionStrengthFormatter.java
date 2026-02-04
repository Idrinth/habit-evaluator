package de.idrinth.habitevaluator.shared.model;

/**
 * Formats emotion strength values as percentage with the corresponding emotion label.
 * A strength of 5 on a "fatigued — energetic" pair becomes "50% energetic".
 * A strength of -3 becomes "30% fatigued".
 * A strength of 0 becomes "0%".
 */
public final class EmotionStrengthFormatter {

    private EmotionStrengthFormatter() {
    }

    public static String format(int strength, String negativeLabel, String positiveLabel) {
        int percentage = Math.abs(strength) * 10;
        if (strength == 0) {
            return "0%";
        } else if (strength < 0) {
            return percentage + "% " + negativeLabel;
        } else {
            return percentage + "% " + positiveLabel;
        }
    }

    public static String format(int strength, EmotionPair pair) {
        if (pair == null) {
            return Math.abs(strength) * 10 + "%";
        }
        return format(strength, pair.getNegativeLabel(), pair.getPositiveLabel());
    }

    public static String formatDouble(double value, String negativeLabel, String positiveLabel) {
        int percentage = (int) Math.round(Math.abs(value) * 10);
        if (value == 0) {
            return "0%";
        } else if (value < 0) {
            return percentage + "% " + negativeLabel;
        } else {
            return percentage + "% " + positiveLabel;
        }
    }
}
