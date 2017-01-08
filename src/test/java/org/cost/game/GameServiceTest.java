package org.cost.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cost.Exceptions;
import org.cost.player.PlayerDataService;
import org.cost.territory.TerritoryDataService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class GameServiceTest {

    private GameService gameService;
    private GameDataService mockGameDataService;
    private PlayerDataService mockPlayerDataService;
    private TerritoryDataService mockTerritoryDataService;

    @Before
    public void setup() {
        mockGameDataService = mock(GameDataService.class);
        mockPlayerDataService = mock(PlayerDataService.class);
        mockTerritoryDataService = mock(TerritoryDataService.class);
        gameService = new GameService(mockGameDataService, mockPlayerDataService, mockTerritoryDataService);
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

    @Test(expected = Exceptions.ConflictException.class)
    public void createGame_returnsFailure_andDoesNotCreateGame_whenGameAlreadyExists() throws Exception {
        when(mockGameDataService.gameExistsWithName("Game Name")).thenReturn(true);
        CreateGameRequest gameRequest = CreateGameRequest.builder().gameName("Game Name").build();

        gameService.createGame(gameRequest);

    }



    @Test
    public void deleteGame_returnsSuccess_andDeletesGame_whenGameExists() throws Exception {
        when(mockGameDataService.gameExistsWithName("gamename")).thenReturn(true);

        gameService.deleteGame("gamename");

        verify(mockGameDataService).deleteGame("gamename");
    }

    @Test(expected = Exceptions.ResourceNotFoundException.class)
    public void deleteGame_returnsResourceNotFound_whenGameDoesNotExist() throws Exception {
        when(mockGameDataService.gameExistsWithName("gamename")).thenReturn(false);

        gameService.deleteGame("gamename");
    }
}
