package com.example.miniquestsenhancedmod.client;

import com.example.miniquestsenhancedmod.QuestData;

/**
 * Client-side cache for the current quest data and the post-reward countdown.
 */
public final class ClientQuestCache {

    private static String questString = "";
    private static QuestData questData = QuestData.empty();
    private static int countdownTicks = 0;

    private ClientQuestCache() {}

    // ── Quest data ────────────────────────────────────────────────────────
    public static void setQuestString(String encoded) {
        questString = (encoded == null) ? "" : encoded;
        questData   = QuestData.parse(questString);
    }

    /** @deprecated kept for callers that still use the old name */
    @Deprecated
    public static void setCurrentQuestKey(String encoded) { setQuestString(encoded); }

    public static String getQuestString()    { return questString; }
    public static QuestData getCurrentQuestData() { return questData; }
    public static boolean hasActiveQuest()   { return !questString.isEmpty() && !questData.isComplete(); }

    // ── Countdown ─────────────────────────────────────────────────────────
    public static void startCountdown()      { countdownTicks = 100; }
    public static boolean isCountingDown()   { return countdownTicks > 0; }
    public static int getCountdownTicks()    { return countdownTicks; }

    /**
     * Returns true exactly once when countdown reaches 0 — caller should fire next quest.
     */
    public static boolean tickCountdown() {
        if (countdownTicks <= 0) return false;
        countdownTicks--;
        return countdownTicks == 0;
    }
}
