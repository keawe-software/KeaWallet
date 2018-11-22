package de.keawe.keawallet;

public final class Levenshtein {

    public static int distance(final String s, final String t) {

        if (s.isEmpty()) return t.length();
        if (t.isEmpty()) return s.length();
        if (s.equals(t)) return 0;

        final int tLength = t.length();
        final int sLength = s.length();

        int[] swap;
        int[] v0 = new int[tLength + 1];
        int[] v1 = new int[tLength + 1];

        for (int i = 0; i < v0.length; i++) v0[i] = i;

        for (int i = 0; i < sLength; i++) {

            v1[0] = (i + 1);
            for (int j = 0; j < tLength; j++) v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + (s.charAt(i) == t.charAt(j) ? 0 : 1)));

            swap = v0;
            v0 = v1;
            v1 = swap;
        }

        return v0[tLength];
    }
}
