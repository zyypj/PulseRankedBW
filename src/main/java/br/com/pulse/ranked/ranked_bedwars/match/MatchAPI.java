package br.com.pulse.ranked.ranked_bedwars.match;

public interface MatchAPI {

    String getMatch(String matchId);
    String getMatchValue(String matchId, String value);
}
