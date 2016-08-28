package org.cost.player;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerNumberServiceTest {

    @Test
    public void getNextPlayerNumber_returnsNextPlayerNumberForAllAvailableNumbers() {
        PlayerNumberService playerNumberService = new PlayerNumberService();

        for (int currentPlayers = 0; currentPlayers<PlayerController.MAX_PLAYERS_ALLOWED_IN_GAME; currentPlayers++) {
            int nextPlayerNumber = playerNumberService.getNextPlayerNumber(currentPlayers);
            assertThat(nextPlayerNumber).isEqualTo(currentPlayers + 1);
        }
    }

    @Test
    public void getNextPlayerNuber_returnsNegativeNumberForPlayerGreaterThanMaxPlayers() {
        PlayerNumberService playerNumberService = new PlayerNumberService();

        int nextPlayerNumber = playerNumberService.getNextPlayerNumber(PlayerController.MAX_PLAYERS_ALLOWED_IN_GAME);

        assertThat(nextPlayerNumber).isEqualTo(-1);
    }

    @Test
    public void getNumberOfPlayersInGame_withListWithOneNullElement_returnsZero() {
        PlayerNumberService playerNumberService = new PlayerNumberService();

        int numberOfPlayersInGame = playerNumberService.getNumberOfPlayersInGame(new ArrayList<>(Arrays.asList(new Player[]{null})));

        assertThat(numberOfPlayersInGame).isEqualTo(0);
    }

    @Test
    public void getNumberOfPlayersInGame_withListWithNonNullElements_returnsNumberOfNonNullElements() {
        PlayerNumberService playerNumberService = new PlayerNumberService();

        ArrayList<Player> players = new ArrayList<>();
        for (int i = 0 ; i < new Double((Math.random() * 100)).intValue(); i++) {
            players.add(new Player());
        }

        assertThat(playerNumberService.getNumberOfPlayersInGame(players)).isEqualTo(players.size());
    }
}