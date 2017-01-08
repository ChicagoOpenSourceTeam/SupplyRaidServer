package org.cost.game;

import org.assertj.core.api.Assertions;
import org.cost.Exceptions;
import org.cost.player.*;
import org.cost.territory.Territory;
import org.cost.territory.TerritoryDataService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;


public class GameServiceTest {

    private GameService gameService;
    private GameDataService mockGameDataService;
    private PlayerDataService mockPlayerDataService;
    private TerritoryDataService mockTerritoryDataService;
    private PlayerTerritoryDataService mockPlayerTerritoryDataService;

    @Before
    public void setup() {
        mockGameDataService = mock(GameDataService.class);
        mockPlayerDataService = mock(PlayerDataService.class);
        mockTerritoryDataService = mock(TerritoryDataService.class);
        mockPlayerTerritoryDataService = mock(PlayerTerritoryDataService.class);
        gameService = new GameService(mockGameDataService, mockPlayerDataService, mockTerritoryDataService, mockPlayerTerritoryDataService);
    }


    @Test
    public void createGame_returnsSuccess_andCreatesGame_whenGameDoesNotExist() throws Exception {
        when(mockGameDataService.gameExistsWithName("Game Name")).thenReturn(false);
        CreateGameRequest gameRequest = CreateGameRequest.builder().gameName("Game Name").build();

        gameService.createGame(gameRequest);

        Game expectedGame = Game.builder()
                .gameName("Game Name")
                .playerTerritories(new ArrayList<>())
                .turnNumber(1).build();
        verify(mockGameDataService).saveGame(expectedGame);
    }

    @Test
    public void createGame_returnsFailure_andDoesNotCreateGame_whenGameAlreadyExists() throws Exception {
        when(mockGameDataService.gameExistsWithName("Game Name")).thenReturn(true);
        CreateGameRequest gameRequest = CreateGameRequest.builder().gameName("Game Name").build();

        try {
            gameService.createGame(gameRequest);
            fail("Expected Conflict Exception");
        } catch (Exception e) {
            Assertions.assertThat(e.getClass()).isEqualTo(Exceptions.ConflictException.class);
            Assertions.assertThat(e.getMessage()).isEqualTo("Game Name Taken");
        }

    }

    @Test   /// is there a way to not use an argument captor, here? ///
    public void createGame_initializesPlayerTerritoriesOwnersToNull_onPost() throws Exception{
        List<Territory> territoryList = Arrays.asList(
                Territory.builder().territoryId(1L).build(),
                Territory.builder().territoryId(2L).build()
        );
        when(mockTerritoryDataService.getListOfTerritoriesOnMap()).thenReturn(territoryList);
        CreateGameRequest gameRequest = CreateGameRequest.builder().gameName("gamename").build();

        gameService.createGame(gameRequest);

        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        verify(mockGameDataService).saveGame(gameArgumentCaptor.capture());
        Game game = gameArgumentCaptor.getValue();

        assertThat(game.getPlayerTerritories().size()).isEqualTo(2);
        assertThat(game.getPlayerTerritories().get(0).getGameName()).isEqualTo("gamename");
        assertThat(game.getPlayerTerritories().get(0).getTerritoryId()).isIn(1L, 2L);
        assertThat(game.getPlayerTerritories().get(0).getPlayerId()).isNull();
    }


    @Test
    public void checkIfGameHasStarted_returnsExpectedResult() throws Exception {
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gameName");
        when(mockGameDataService.findGameByName("gameName")).thenReturn(Game.builder().started(true).build());

        GameResponse gameResponse = gameService.checkIfGameHasStarted(mockHttpSession);

        assertThat(gameResponse.isGameStarted()).isEqualTo(true);
    }



    @Test
    public void deleteGame_returnsSuccess_andDeletesGame_whenGameExists() throws Exception {
        when(mockGameDataService.gameExistsWithName("gamename")).thenReturn(true);

        gameService.deleteGame("gamename");

        verify(mockGameDataService).deleteGame("gamename");
    }

    @Test
    public void deleteGame_returnsResourceNotFound_whenGameDoesNotExist() throws Exception {
        when(mockGameDataService.gameExistsWithName("gamename")).thenReturn(false);

        try {
            gameService.deleteGame("gamename");
            fail("Expected Resource Not Found Exception");
        } catch (Exception e) {
            Assertions.assertThat(e.getClass()).isEqualTo(Exceptions.ResourceNotFoundException.class);
            Assertions.assertThat(e.getMessage()).isEqualTo("Game Does Not Exist");
        }
    }



    @Test
    public void startGameRequest_setsGameStarted() throws Exception{
        List<Player> players = Arrays.asList(new Player(), new Player());
        Game game = Game.builder().gameName("gamename").players(players).build();
        when(mockGameDataService.findGameByName("gamename")).thenReturn(game);
        List<Territory> territories = generateTerritoriesForTest();
        when(mockTerritoryDataService.getListOfTerritoriesOnMap()).thenReturn(territories);
        when(mockPlayerTerritoryDataService.getTerritoriesInGame("gamename")).thenReturn(playerTerritoriesForTest());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        gameService.startGame(session);

        verify(mockGameDataService).saveGame(game);
        assertTrue(game.isStarted());
    }








    private List<Territory> generateTerritoriesForTest() {
        return Arrays.asList(
                Territory.builder().supply(2).east(1L).north(2L).south(3L).west(4L).territoryId(5L).build(),
                Territory.builder().supply(2).east(6L).north(7L).south(8L).west(9L).territoryId(10L).build(),
                Territory.builder().supply(2).east(11L).north(12L).south(13L).west(14L).territoryId(15L).build(),
                Territory.builder().supply(2).east(16L).north(17L).south(18L).west(19L).territoryId(20L).build());
    }

    private List<PlayerTerritory> playerTerritoriesForTest() {
        List<PlayerTerritory> playerTerritories = new ArrayList<>();
        for(int id=1; id<=20; id++) {
            playerTerritories.add(PlayerTerritory.builder().territoryId((long)id).build());
        }
        return playerTerritories;

    }




}
