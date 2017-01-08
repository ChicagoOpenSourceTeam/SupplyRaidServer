package org.cost.player;

import org.cost.Exceptions;
import org.cost.game.Game;
import org.cost.game.GameDataService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class PlayerServiceTest {

    private GameDataService mockGameDataService;
    private PlayerDataService mockPlayerDataService;
    private PlayerNumberService mockPlayerNumberService;
    private PlayerService playerService;

    @Before
    public void setup() {
        mockGameDataService = mock(GameDataService.class);
        mockPlayerDataService = mock(PlayerDataService.class);
        mockPlayerNumberService = mock(PlayerNumberService.class);
        playerService = new PlayerService(mockPlayerDataService, mockGameDataService, mockPlayerNumberService);
    }

    @Test
    public void createPlayer_addsPlayerToGame() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player[]{null}));
        when(mockPlayerDataService.findPlayersInGameByGameName("Excalibur")).thenReturn(players);
        when(mockGameDataService.findGameByName(anyString())).thenReturn(new Game());
        when(mockPlayerNumberService.getNumberOfPlayersInGame(players)).thenReturn(66);
        when(mockPlayerNumberService.getNextPlayerNumber(66)).thenReturn(999);

        playerService.addPlayerToGame(CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build(), new MockHttpSession());

        Player expectedPlayer = Player.builder().name("zxmbies").gameName("Excalibur").playerNumber(999).remainingActions(3).build();
        verify(mockPlayerDataService).savePlayer(expectedPlayer);
    }

    @Test
    public void createPlayer_returnsConflict_whenFourOrMorePlayersInGame() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player(), new Player(), new Player(), new Player()));
        when(mockPlayerDataService.findPlayersInGameByGameName("Excalibur")).thenReturn(players);
        when(mockGameDataService.findGameByName(anyString())).thenReturn(new Game());

        try {
            playerService.addPlayerToGame(CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build(), null);
            fail("Expected conflict exception");
        } catch (Exception e) {
            assertThat(e.getClass()).isEqualTo(Exceptions.ConflictException.class);
            assertThat(e.getMessage()).isEqualTo("Game Lobby is Full");
        }
    }

    @Test
    public void createPlayer_returnsConflict_whenPlayerAlreadyInGame() throws Exception {
        Player player = Player.builder().name("zxmbies").build();
        ArrayList<Player> players = new ArrayList<>(Collections.singletonList(player));
        when(mockPlayerDataService.findPlayersInGameByGameName("Excalibur")).thenReturn(players);
        when(mockGameDataService.findGameByName(anyString())).thenReturn(new Game());

        try {
            playerService.addPlayerToGame(CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build(), null);
            fail("Expected conflict exception");
        } catch (Exception e) {
            assertThat(e.getClass()).isEqualTo(Exceptions.ConflictException.class);
            assertThat(e.getMessage()).isEqualTo("Player name already taken");
        }
    }

    @Test
    public void createPlayer_returnsResourceNotFound_whenGameDoesNotExist() throws Exception {
        when(mockGameDataService.findGameByName(anyString())).thenReturn(null);

        try {
            playerService.addPlayerToGame(CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build(), null);
            fail("Expected conflict exception");
        } catch (Exception e) {
            assertThat(e.getClass()).isEqualTo(Exceptions.ResourceNotFoundException.class);
            assertThat(e.getMessage()).isEqualTo("Game not found");
        }
    }

    @Test
    public void postPlayer_returnsError_whenGameStarted() throws Exception {
        Game game = Game.builder().gameName("Excalibur").started(true).build();
        when(mockPlayerDataService.findPlayersInGameByGameName("Excalibur")).thenReturn(new ArrayList<>(
                Arrays.asList(
                        Player.builder().name("a").build(),
                        Player.builder().name("b").build(),
                        Player.builder().name("c").build())));
        when(mockGameDataService.findGameByName("Excalibur")).thenReturn(game);

        try {
            playerService.addPlayerToGame(CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build(), null);
            fail("Expected conflict exception");
        } catch (Exception e) {
            assertThat(e.getClass()).isEqualTo(Exceptions.ConflictException.class);
            assertThat(e.getMessage()).isEqualTo("The game has already started");
        }
    }

    @Test
    public void postPlayer_savesGameToHttpSession() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player[]{null}));
        when(mockPlayerDataService.findPlayersInGameByGameName("Excalibur")).thenReturn(players);
        when(mockGameDataService.findGameByName(anyString())).thenReturn(new Game());

        MockHttpSession httpSession = new MockHttpSession();
        playerService.addPlayerToGame(CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build(), httpSession);

        assertThat(httpSession.getAttribute("game_name")).isEqualTo("Excalibur");
    }

    @Test
    public void postPlayer_savesPlayerNumberToHttpSession() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player[]{null}));
        when(mockPlayerDataService.findPlayersInGameByGameName("Excalibur")).thenReturn(players);
        when(mockGameDataService.findGameByName(anyString())).thenReturn(new Game());
        when(mockPlayerNumberService.getNumberOfPlayersInGame(players)).thenReturn(0);
        when(mockPlayerNumberService.getNextPlayerNumber(0)).thenReturn(1);

        MockHttpSession httpSession = new MockHttpSession();
        playerService.addPlayerToGame(CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build(), httpSession);

        assertThat(httpSession.getAttribute("player_number")).isEqualTo(1);
    }

}