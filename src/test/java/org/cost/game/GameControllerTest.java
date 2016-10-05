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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Test
    public void startGameRequest_returnsOK_whenGameIsValid() throws Exception{
        Game game = new Game();
        game.setGameName("gamename");
        List<Player> players = Arrays.asList(new Player(), new Player());
        game.setPlayers(players);
        when(mockRepository.findOne("gamename")).thenReturn(game);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");

        List<Territory> territories = Arrays.asList(
                Territory.builder().supply(2).territoryId(1L).build(), Territory.builder().supply(2).territoryId(1L).build(),
                Territory.builder().supply(2).territoryId(1L).build(), Territory.builder().supply(2).territoryId(1L).build());
        when(mockTerritoryRepository.findAll()).thenReturn(territories);

        List<PlayerTerritory> playerTerritories =  Arrays.asList(
                PlayerTerritory.builder().territoryId(1L).build(),
                PlayerTerritory.builder().territoryId(2L).build(),
                PlayerTerritory.builder().territoryId(3L).build(),
                PlayerTerritory.builder().territoryId(4L).build());
        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(playerTerritories);

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
        List<Player> players = Collections.singletonList(new Player());
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
        List<Player> players = Arrays.asList(Player.builder().playerId(10L).build(), Player.builder().playerId(20L).build());
        game.setPlayers(players);
        when(mockRepository.findOne("gamename")).thenReturn(game);

        List<Territory> territories = Arrays.asList(
                Territory.builder().supply(2).territoryId(1L).build(), Territory.builder().supply(2).territoryId(2L).build(),
                Territory.builder().supply(2).territoryId(3L).build(), Territory.builder().supply(2).territoryId(4L).build());
        when(mockTerritoryRepository.findAll()).thenReturn(territories);


        List<PlayerTerritory> playerTerritories = Arrays.asList(
                PlayerTerritory.builder().territoryId(1L).build(),
                PlayerTerritory.builder().territoryId(2L).build(),
                PlayerTerritory.builder().territoryId(3L).build(),
                PlayerTerritory.builder().territoryId(4L).build());

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

    @Test
    public void startGame_assignsTerritoriesAdjacentToSupplyDepots_toControllingPlayers() throws Exception {
        Game game = new Game();
        game.setGameName("gamename");
        List<Player> players = Arrays.asList(Player.builder().playerId(10L).build(), Player.builder().playerId(20L).build());
        game.setPlayers(players);
        when(mockRepository.findOne("gamename")).thenReturn(game);

        List<Territory> territories = Arrays.asList(
                Territory.builder().supply(2).east(1L).north(2L).south(3L).west(4L).territoryId(5L).build(),
                Territory.builder().supply(2).east(6L).north(7L).south(8L).west(9L).territoryId(10L).build(),
                Territory.builder().supply(2).east(11L).north(12L).south(13L).west(14L).territoryId(15L).build(),
                Territory.builder().supply(2).east(16L).north(17L).south(18L).west(19L).territoryId(20L).build());
                when(mockTerritoryRepository.findAll()).thenReturn(territories);


        List<PlayerTerritory> playerTerritories = Arrays.asList(
                PlayerTerritory.builder().territoryId(1L).build(),
                PlayerTerritory.builder().territoryId(2L).build(),
                PlayerTerritory.builder().territoryId(3L).build(),
                PlayerTerritory.builder().territoryId(4L).build(),
                PlayerTerritory.builder().territoryId(5L).build(),
                PlayerTerritory.builder().territoryId(6L).build(),
                PlayerTerritory.builder().territoryId(7L).build(),
                PlayerTerritory.builder().territoryId(8L).build(),
                PlayerTerritory.builder().territoryId(9L).build(),
                PlayerTerritory.builder().territoryId(10L).build(),
                PlayerTerritory.builder().territoryId(11L).build(),
                PlayerTerritory.builder().territoryId(12L).build(),
                PlayerTerritory.builder().territoryId(13L).build(),
                PlayerTerritory.builder().territoryId(14L).build(),
                PlayerTerritory.builder().territoryId(15L).build(),
                PlayerTerritory.builder().territoryId(16L).build(),
                PlayerTerritory.builder().territoryId(17L).build(),
                PlayerTerritory.builder().territoryId(18L).build(),
                PlayerTerritory.builder().territoryId(19L).build(),
                PlayerTerritory.builder().territoryId(20L).build());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(playerTerritories);


        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk());

        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPlayerTerritoryRepository).save(listArgumentCaptor.capture());
        List<PlayerTerritory> value = listArgumentCaptor.getValue();

        //assert that playerId of 5L is same as playerId of 1L, and so on.

        Long playerId = value
                .stream()
                .filter(pt -> pt.getTerritoryId().equals(5L))
                .findFirst()
                .get().getPlayerId();

        List<Long> territoriesWithPlayerId = value
                .stream()
                .filter(pt -> pt.getPlayerId().equals(playerId))
                .map(PlayerTerritory::getTerritoryId)
                .collect(Collectors.toList());

        assertThat(territoriesWithPlayerId).contains(1L, 2L, 3L, 4L);


        Long secondPlayerId = value
                .stream()
                .filter(pt -> pt.getTerritoryId().equals(15L))
                .findFirst()
                .get().getPlayerId();

        territoriesWithPlayerId = value
                .stream()
                .filter(pt -> pt.getPlayerId().equals(secondPlayerId))
                .map(PlayerTerritory::getTerritoryId)
                .collect(Collectors.toList());


        assertThat(territoriesWithPlayerId).contains(11L,12L,13L,14L);




    }

}