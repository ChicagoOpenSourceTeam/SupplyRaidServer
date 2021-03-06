package org.cost.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cost.Exceptions;
import org.cost.player.*;
import org.cost.territory.Territory;
import org.cost.territory.TerritoryRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
    private SuppliedStatusService mockSuppliedStatusService;
    GameService mockGameService;


    @Before
    public void setup() {
        mockRepository = mock(GameRepository.class);
        mockTerritoryRepository = mock(TerritoryRepository.class);
        mockPlayerTerritoryRepository = mock(PlayerTerritoryRepository.class);
        mockSuppliedStatusService = mock(SuppliedStatusService.class);
        mockGameService = mock(GameService.class);
        GameController gameController = new GameController(mockRepository, mockTerritoryRepository, mockPlayerTerritoryRepository, mockSuppliedStatusService, mockGameService);
        mockMvc = MockMvcBuilders.standaloneSetup(gameController).build();
    }

    @Test
    public void createGame_callsGameService_andReturnsOkStatus() throws Exception {
        MockHttpSession mockHttpSession = new MockHttpSession();
        ObjectMapper objectMapper = new ObjectMapper();
        CreateGameRequest gameRequest = CreateGameRequest.builder().gameName("gamename").build();
        String content = objectMapper.writeValueAsString(gameRequest);

        mockMvc.perform(post("/game").contentType(MediaType.APPLICATION_JSON).content(content).session(mockHttpSession))
                .andExpect(status().isOk());

        verify(mockGameService).createGame(CreateGameRequest.builder().gameName("gamename").build());
    }

    @Test
    public void createGame_callsGameService_andThrowsException() throws Exception {
        doThrow(new Exceptions.ConflictException("Game Name Taken")).when(mockGameService).createGame(any());
        ObjectMapper objectMapper = new ObjectMapper();
        CreateGameRequest gameRequest = CreateGameRequest.builder().gameName("gamename").build();
        String content = objectMapper.writeValueAsString(gameRequest);

        MockHttpSession mockHttpSession = new MockHttpSession();

        mockMvc.perform(post("/game").contentType(MediaType.APPLICATION_JSON).content(content).session(mockHttpSession))
                .andExpect(status().isConflict());

    }




    @Test
    public void getGameEndpoint_callsService_andReturnsGame() throws Exception {
        MockHttpSession mockHttpSession = new MockHttpSession();
        ObjectMapper objectMapper = new ObjectMapper();
        CreateGameRequest gameRequest = CreateGameRequest.builder().gameName("gamename").build();
        String content = objectMapper.writeValueAsString(gameRequest);

        mockMvc.perform(get("/game").contentType(MediaType.APPLICATION_JSON).content(content).session(mockHttpSession))
                .andExpect(status().isOk());

        verify(mockGameService).checkIfGameHasStarted(mockHttpSession);
    }




    @Test
    public void deleteGameEndpoint_callsGameService() throws Exception{
        mockMvc.perform(delete("/game/gamename"))
                .andExpect(status().isOk());

        verify(mockGameService).deleteGame("gamename");
    }

    @Test
    public void deleteGame_callsGameService_andThrowsException() throws Exception{
        doThrow(new Exceptions.ResourceNotFoundException("Game Not Found")).when(mockGameService).deleteGame(any());

        mockMvc.perform(delete("/game/gamename"))
                .andExpect(status().isConflict());
    }


    @Test
    public void postToStartGameEndpoint_callsStartGameService() throws Exception {
        MockHttpSession mockHttpSession = new MockHttpSession();

        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(mockHttpSession))
                .andExpect(status().isOk());

        verify(mockGameService).startGame(mockHttpSession);
    }



//    @Test
//    public void startGameRequest_returnsOK_whenGameIsValid() throws Exception{
//        List<Player> players = Arrays.asList(new Player(), new Player());
//        Game game = Game.builder().gameName("gamename").players(players).build();
//        when(mockRepository.findOne("gamename")).thenReturn(game);
//        List<Territory> territories = generateTerritoriesForTest();
//        when(mockTerritoryRepository.findAll()).thenReturn(territories);
//        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(generatePlayerTerritoriesForTest());
//
//        MockHttpSession session = new MockHttpSession();
//        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
//        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
//                .andExpect(status().isOk());
//
//        verify(mockRepository).save(game);
//        assertTrue(game.isStarted());
//    }


    @Test
    public void startGameRequest_callsMarkUnsuppliedThenSupplied_inSupplyService() throws Exception {
        List<Player> players = Arrays.asList(Player.builder().playerId(10L).build(), Player.builder().playerId(20L).build());
        Game game = Game.builder().gameName("gamename").players(players).build();
        when(mockRepository.findOne("gamename")).thenReturn(game);
        List<Territory> territories = generateTerritoriesForTest();
        when(mockTerritoryRepository.findAll()).thenReturn(territories);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        List<PlayerTerritory> playerTerritories = generatePlayerTerritoriesForTest();
        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(playerTerritories);

        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session));

        InOrder inOrder = inOrder(mockSuppliedStatusService);
        inOrder.verify(mockSuppliedStatusService).markUnsupplied();
        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        inOrder.verify(mockSuppliedStatusService).markSupplied(listArgumentCaptor.capture(), eq(playerTerritories));

        List<PlayerTerritory> value = (List<PlayerTerritory>) listArgumentCaptor.getValue();
        assertThat(value
                .stream()
                .map(PlayerTerritory::getTerritoryId)
                .collect(Collectors.toList())).containsExactlyInAnyOrder(5L, 10L, 15L, 20L);
    }

//    @Test
//    public void startGameRequest_returnsNotFound_whenGameIsNull() throws Exception{
//        when(mockRepository.findOne("gamename")).thenReturn(null);
//        MockHttpSession session = new MockHttpSession();
//        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
//
//        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
//                .andExpect(status().isNotFound());
//    }

    @Test
    public void startGameRequest_returnsConflict_whenLessThan2Players() throws Exception{
        List<Player> players = Collections.singletonList(new Player());
        Game game = Game.builder().gameName("gamename").players(players).build();

        when(mockRepository.findOne("gamename")).thenReturn(game);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isConflict());
    }


    @Test
    public void startGameRequest_returnsConflict_whenGameAlreadyStarted() throws Exception {
        List<Player> players = Arrays.asList(new Player(), new Player());
        Game game = Game.builder().gameName("gamename").players(players).started(true).build();

        when(mockRepository.findOne("gamename")).thenReturn(game);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");

        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isConflict());
    }

    @Test
    public void getGameEndpoint_returnsGameHasNotStarted_whenFalse() throws Exception{
        Game game = Game.builder().started(false).build();
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
    public void startGame_assignsSupplyDepotsToPlayers_andPuts8TroopsOnDepots() throws Exception {
        List<Player> players = Arrays.asList(Player.builder().playerId(10L).build(), Player.builder().playerId(20L).build());
        Game game = Game.builder().gameName("gamename").players(players).build();
        when(mockRepository.findOne("gamename")).thenReturn(game);
        List<Territory> territories = generateTerritoriesForTest();
        when(mockTerritoryRepository.findAll()).thenReturn(territories);


        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(generatePlayerTerritoriesForTest());
        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk());

        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPlayerTerritoryRepository).save(listArgumentCaptor.capture());
        List<PlayerTerritory> value = listArgumentCaptor.getValue();

        assertThat(value
                .stream()
                .filter(p -> p.getTroops()==8)
                .filter(p -> p.getPlayerId().equals(10L))
                .count()).isEqualTo(2);

        assertThat(value
                .stream()
                .filter(p -> p.getTroops()==8)
                .filter(p -> p.getPlayerId().equals(20L))
                .count()).isEqualTo(2);
    }


    @Test
    public void startGame_assignsTerritoriesAdjacentToSupplyDepots_toControllingPlayers() throws Exception {
        List<Player> players = Arrays.asList(Player.builder().playerId(10L).build(), Player.builder().playerId(20L).build());
        Game game = Game.builder().gameName("gamename").players(players).build();
        when(mockRepository.findOne("gamename")).thenReturn(game);
        List<Territory> territories = generateTerritoriesForTest();
        when(mockTerritoryRepository.findAll()).thenReturn(territories);
        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(generatePlayerTerritoriesForTest());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk());

        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPlayerTerritoryRepository).save(listArgumentCaptor.capture());
        List<PlayerTerritory> value = listArgumentCaptor.getValue();

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

    @Test
    public void startGame_initializesNonSupplyTerritories_toHave12SurroundingTroops() throws Exception {
        List<Player> players = Arrays.asList(Player.builder().playerId(10L).build(), Player.builder().playerId(20L).build());
        Game game = Game.builder().gameName("gamename").players(players).build();
        when(mockRepository.findOne("gamename")).thenReturn(game);
        List<Territory> territories = generateTerritoriesForTest();
        when(mockTerritoryRepository.findAll()).thenReturn(territories);
        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(generatePlayerTerritoriesForTest());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        mockMvc.perform(post("/game/start").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk());

        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPlayerTerritoryRepository).save(listArgumentCaptor.capture());
        List<PlayerTerritory> value = listArgumentCaptor.getValue();

        int totalTroops = 0;
        for (PlayerTerritory pt:value) {
            totalTroops += pt.getTroops();
        }
        assertThat(totalTroops).isEqualTo(80); // (8 troops per depot + 12 troops per adjacent)*2depotsperplayer*2players
    }







    private List<PlayerTerritory> generatePlayerTerritoriesForTest() {
        List<PlayerTerritory> playerTerritories = new ArrayList<>();
        for(int id=1; id<=20; id++) {
            playerTerritories.add(PlayerTerritory.builder().territoryId((long)id).build());
        }
        return playerTerritories;

    }

    private List<Territory> generateTerritoriesForTest() {
        return Arrays.asList(
                Territory.builder().supply(2).east(1L).north(2L).south(3L).west(4L).territoryId(5L).build(),
                Territory.builder().supply(2).east(6L).north(7L).south(8L).west(9L).territoryId(10L).build(),
                Territory.builder().supply(2).east(11L).north(12L).south(13L).west(14L).territoryId(15L).build(),
                Territory.builder().supply(2).east(16L).north(17L).south(18L).west(19L).territoryId(20L).build());
    }


}
