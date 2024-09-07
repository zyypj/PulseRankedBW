package br.com.pulse.ranked.misc.fourS.match;

public interface MatchAPI {

    String getMatch(String matchId);
    String getMatchValue(String matchId, String value);
}
