package org.cost.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cost.player.Player;
import org.cost.player.PlayerController;
import org.cost.player.PlayerTerritory;
import org.cost.player.PlayerTerritoryRepository;
import org.cost.territory.Territory;
import org.cost.territory.TerritoryRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GameControllerTest {

    private MockMvc mockMvc;
    private GameRepository mockRepository;

    TerritoryRepository mockTerritoryRepository;
    PlayerTerritoryRepository mockPlayerTerritoryRepository;


    @Before
    public void setup() {
        mockRepository = mock(GameRepository.class);
        mockTerritoryRepository = mock(TerritoryRepository.class);
        mockPlayerTerritoryRepository = mock(PlayerTerritoryRepository.class);
        GameController gameController = new GameController(mockRepository, mockTerritoryRepository, mockPlayerTerritoryRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(gameController).build();
    }

    @Test
    public void createGame_returnsSuccess_andCreatesGame_whenGameDoesNotExist() throws Exception {
        when(mockRepository.exists("Game Name")).thenReturn(false);
        ObjectMapper objectMapper = new ObjectMapper();
        GameController.GameRequest gameRequest = GameController.GameRequest.builder().gameName("Game Name").build();
        String content = objectMapper.writeValueAsString(gameRequest);

        mockMvc.perform(post("/game").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isOk());

        Game game = new Game();
        game.setGameName("Game Name");
        game.setPlayerTerritories(new ArrayList<>());
        verify(mockRepository).save(game);
    }

    @Test
    public void createGame_returnsFailure_andDoesNotCreateGame_whenGameAlreadyExists() throws Exception {
        when(mockRepository.exists("Game Name")).thenReturn(true);
        ObjectMapper objectMapper = new ObjectMapper();
        GameController.GameRequest gameRequest = GameController.GameRequest.builder().gameName("Game Name").build();
        String content = objectMapper.writeValueAsString(gameRequest);

        mockMvc.perform(post("/game").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isConflict());

        verify(mockRepository, times(0)).save(any(Game.class));
    }

    @Test
    public void deleteGame_returnsSuccess_andDeletesGame_whenGameExists() throws Exception {
        when(mockRepository.exists("gamename")).thenReturn(true);

        mockMvc.perform(delete("/game/gamename"))
                .andExpect(status().isOk());

        verify(mockRepository).delete("gamename");
    }

    @Test
    public void deleteGame_returnsResourceNotFound_whenGameDoesNotExist() throws Exception {
        when(mockRepository.exists("gamename")).thenReturn(false);

        mockMvc.perform(delete("/game/gamename"))
                .andExpect(status().isNotFound());

        verify(mockRepository, times(0)).delete("gamename");
    }

    //awaitingacceptance
    @Test
    public void startGameRequest_returnsOK_whenGameIsValid() throws Exception{
        Game game = new Game();
        game.setGameName("gamename");
        Player player1 = new Player();
        Player player2 = new Player();
        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        game.setPlayers(players);

        when(mockRepository.findOne("gamename")).thenReturn(game);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");

        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk());

        verify(mockRepository).save(game);
        assertThat(game.getStarted()).isEqualTo(1);
    }


    @Test
    public void startGameRequest_returnsNotFound_whenGameIsNull() throws Exception{
        when(mockRepository.findOne("gamename")).thenReturn(null);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");

        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    public void startGameRequest_returnsConflict_whenLessThan2Players() throws Exception{
        Game game = new Game();
        game.setGameName("gamename");
        Player player1 = new Player();
        List<Player> players = new ArrayList<>();
        players.add(player1);
        game.setPlayers(players);
        when(mockRepository.findOne("gamename")).thenReturn(game);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");

        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isConflict());
    }


    @Test
    public void getGameEndpoint_returnsGameHasStarted_whenTrue() throws Exception{
        Game game = new Game();
        game.setStarted(1);
        when(mockRepository.findOne("gamename")).thenReturn(game);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");

        String JSONResponse = mockMvc.perform(get("/game").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals(
                "{\n" +
                        "  \"gameStarted\": true\n" +
                        "}", JSONResponse, JSONCompareMode.LENIENT);
    }

    @Test
    public void getGameEndpoint_returnsGameHasNotStarted_whenFalse() throws Exception{
        Game game = new Game();
        game.setStarted(0);
        when(mockRepository.findOne("gamename")).thenReturn(game);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");

        String JSONResponse = mockMvc.perform(get("/game").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals(
                "{\n" +
                        "  \"gameStarted\": false\n" +
                        "}", JSONResponse, JSONCompareMode.LENIENT);
    }


    @Test
    public void createGame_initializesPlayerTerritoriesOwnerstoNull_onPost() throws Exception{
        List<Territory> territoryList = new ArrayList<>(Arrays.asList(Territory.builder().territoryId(1L).build(),
                Territory.builder().territoryId(2L).build()));

        when(mockTerritoryRepository.findAll()).thenReturn(territoryList);

        ObjectMapper objectMapper = new ObjectMapper();
        GameController.GameRequest gameRequest = GameController.GameRequest.builder().gameName("gamename").build();
        String content = objectMapper.writeValueAsString(gameRequest);

        mockMvc.perform(post("/game").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isOk());

        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        verify(mockRepository).save(gameArgumentCaptor.capture());

        Game game = gameArgumentCaptor.getValue();

        assertThat(game.getPlayerTerritories().size()).isEqualTo(2);
        assertThat(game.getPlayerTerritories().get(0).getGameName()).isEqualTo("gamename");
        assertThat(game.getPlayerTerritories().get(0).getTerritoryId()).isIn(1L, 2L);
        assertThat(game.getPlayerTerritories().get(0).getPlayerId()).isNull();
    }

    @Test
    public void startGame_assignsSupplyDepotsToPlayers() throws Exception {
        Game game = new Game();
        game.setGameName("gamename");
        Player player1 = new Player();
        player1.setPlayerId(10L);
        Player player2 = new Player();
        player2.setPlayerId(20L);
        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        game.setPlayers(players);
        when(mockRepository.findOne("gamename")).thenReturn(game);

        Territory territory1 = new Territory();
        territory1.setSupply(2);
        Territory territory2 = new Territory();
        territory2.setSupply(2);
        Territory territory3 = new Territory();
        territory3.setSupply(2);
        Territory territory4 = new Territory();
        territory4.setSupply(2);
        List<Territory> territories = new ArrayList<>();
        territories.add(territory1);
        territories.add(territory2);
        territories.add(territory3);
        territories.add(territory4);
        when(mockTerritoryRepository.findAll()).thenReturn(territories);

        PlayerTerritory playerTerritory1 = new PlayerTerritory();
        PlayerTerritory playerTerritory2 = new PlayerTerritory();
        PlayerTerritory playerTerritory3 = new PlayerTerritory();
        PlayerTerritory playerTerritory4 = new PlayerTerritory();
        List<PlayerTerritory> playerTerritories = new ArrayList<>();
        playerTerritories.add(playerTerritory1);
        playerTerritories.add(playerTerritory2);
        playerTerritories.add(playerTerritory3);
        playerTerritories.add(playerTerritory4);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(playerTerritories);

        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk());

        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPlayerTerritoryRepository).save(listArgumentCaptor.capture());

        List<PlayerTerritory> value = listArgumentCaptor.getValue();

        assertThat(value
                .stream()
                .filter(p -> p.getPlayerId().equals(10L))
                .count()).isEqualTo(2);

        assertThat(value
                .stream()
                .filter(p -> p.getPlayerId().equals(20L))
                .count()).isEqualTo(2);
    }
}