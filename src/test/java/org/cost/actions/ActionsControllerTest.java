package org.cost.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cost.game.Game;
import org.cost.game.GameRepository;
import org.cost.player.Player;
import org.cost.player.PlayerRepository;
import org.cost.player.PlayerTerritory;
import org.cost.player.PlayerTerritoryRepository;
import org.cost.territory.Territory;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cost.player.PlayerController.SESSION_GAME_NAME_FIELD;
import static org.cost.player.PlayerController.SESSION_PLAYER_NUMBER_FIELD;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ActionsControllerTest {

    private MockMvc mockMvc;
    private GameRepository mockGameRepository;
    private PlayerRepository mockPlayerRepository;
    private PlayerTerritoryRepository mockPlayerTerritoryRepository;

    @Before
    public void setup() {
        mockGameRepository = mock(GameRepository.class);
        mockPlayerRepository = mock(PlayerRepository.class);
        mockPlayerTerritoryRepository = mock(PlayerTerritoryRepository.class);
        ActionsController actionsController = new ActionsController(mockGameRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(actionsController).build();
    }

    @Test
    public void skipAction_decrementsCurrentActionNumber_whenPlayerIsActivePlayer() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 2);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        Game game = new Game();
        game.setTurnNumber(5);
        Player thisPlayer = Player.builder().playerNumber(2).remainingActions(3).build();
        game.setPlayers(Arrays.asList(new Player(), thisPlayer, new Player()));
        when(mockGameRepository.findOne("gamename")).thenReturn(game);

        String contentAsString = mockMvc.perform(post("/actions/skip-action").contentType(MediaType.APPLICATION_JSON).session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ;

        verify(mockGameRepository).save(game);
        assertThat(thisPlayer.getRemainingActions()).isEqualTo(2);
        assertEquals("{\n" +
                "  \"actionsRemaining\": 2\n" +
                "}", contentAsString, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void skipAction_returnsForbiddenStatus_whenPlayerIsNotActivePlayer() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 3);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        when(mockGameRepository.findOne("gamename")).thenReturn(Game.builder()
                .turnNumber(1)
                .players(
                        Arrays.asList(
                                new Player(),
                                Player.builder()
                                        .playerNumber(2)
                                        .remainingActions(2)
                                        .build(),
                                new Player()))
                .build());

        mockMvc.perform(post("/actions/skip-action").contentType(MediaType.APPLICATION_JSON).session(session)).andExpect(status().isForbidden());

        verify(mockGameRepository, times(0)).save(any(Game.class));
    }

    @Test
    public void skipAction_changesTurn_andSetsRemainingActionsToThree_whenRemainingActionsAreOne() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 2);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        Game game = new Game();
        game.setTurnNumber(5);
        Player thisPlayer = Player.builder().playerNumber(2).remainingActions(1).build();
        game.setPlayers(Arrays.asList(new Player(), thisPlayer, new Player()));
        when(mockGameRepository.findOne("gamename")).thenReturn(game);

        String contentAsString = mockMvc.perform(post("/actions/skip-action").contentType(MediaType.APPLICATION_JSON).session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ;

        verify(mockGameRepository).save(game);
        assertThat(game.getTurnNumber()).isEqualTo(6);
        assertThat(thisPlayer.getRemainingActions()).isEqualTo(3);
        assertEquals("{\n" +
                "  \"actionsRemaining\": 0\n" +
                "}", contentAsString, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void skipAction_returnsNotFound_whenGameNotFound() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 3);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        when(mockGameRepository.findOne("gamename")).thenReturn(null);

        mockMvc.perform(post("/actions/skip-action").contentType(MediaType.APPLICATION_JSON).session(session)).andExpect(status().isNotFound());
    }

    @Test
    public void moveTroops_changesTroopNumbersAndReducesActionCount_whenBothTerritoriesOwnedByPlayerAndMovingTerritoryHasMovedPlusOneTroops() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 1);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        PlayerTerritory playerTerritoryOne = PlayerTerritory.builder().id(1L).playerId(1L).territoryId(7L).troops(10).territory(Territory.builder().south(9L).build()).build();
        PlayerTerritory playerTerritoryTwo = PlayerTerritory.builder().id(2L).playerId(1L).territoryId(9L).troops(2).territory(Territory.builder().north(7L).build()).build();
        Player player = Player.builder()
                .playerNumber(1)
                .playerTerritoriesList(Arrays.asList(playerTerritoryOne, playerTerritoryTwo))
                .remainingActions(2)
                .build();
        Game game = Game.builder()
                .players(Arrays.asList(player))
                .playerTerritories(Collections.emptyList())
                .turnNumber(1)
                .build();
        when(mockGameRepository.findOne("gamename")).thenReturn(game);


        ActionsController.MoveRequest moveRequest = ActionsController.MoveRequest.builder()
                .moveFrom(7L)
                .moveTo(9L)
                .numberOfTroops(5)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String request = objectMapper.writeValueAsString(moveRequest);

        when(mockGameRepository.save(game)).thenReturn(game);

        String contentAsString = mockMvc.perform(post("/actions/move-troops").contentType(MediaType.APPLICATION_JSON).content(request).session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        verify(mockGameRepository).save(game);
        assertThat(playerTerritoryOne.getTroops()).isEqualTo(5);
        assertThat(playerTerritoryTwo.getTroops()).isEqualTo(7);
        assertThat(player.getRemainingActions()).isEqualTo(1);

        assertEquals("{\n" +
                "  \"actionsRemaining\": 1,\n" +
                "  \"territories\": []\n" +
                "}", contentAsString, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void moveTroops_returnsForbiddenStatus_whenPlayerIsNotActive() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 1);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        Game game = Game.builder()
                .players(Arrays.asList(new Player(), new Player()))
                .turnNumber(2)
                .build();

        when(mockGameRepository.findOne("gamename")).thenReturn(game);

        ObjectMapper objectMapper = new ObjectMapper();
        String request = objectMapper.writeValueAsString(new ActionsController.MoveRequest());

        mockMvc.perform(post("/actions/move-troops").contentType(MediaType.APPLICATION_JSON).content(request).session(session)).andExpect(status().isForbidden());
    }

    @Test
    public void moveTroops_returnsForbidden_whenPlayerDoesNotOwnStartingTerritory() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 1);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        PlayerTerritory playerTerritoryOne = PlayerTerritory.builder().id(1L).playerId(1L).territoryId(7L).troops(10).territory(Territory.builder().south(9L).build()).build();
        PlayerTerritory playerTerritoryTwo = PlayerTerritory.builder().id(2L).playerId(1L).territoryId(9L).troops(2).territory(Territory.builder().north(7L).build()).build();
        Player player = Player.builder()
                .playerNumber(1)
                .playerTerritoriesList(Arrays.asList(playerTerritoryOne, playerTerritoryTwo))
                .remainingActions(2)
                .build();

        Game game = Game.builder()
                .players(Arrays.asList(player))
                .turnNumber(1)
                .build();

        when(mockGameRepository.findOne("gamename")).thenReturn(game);

        ObjectMapper objectMapper = new ObjectMapper();
        ActionsController.MoveRequest moveRequest = ActionsController.MoveRequest.builder()
                .moveFrom(6L)
                .moveTo(9L)
                .numberOfTroops(5)
                .build();
        String request = objectMapper.writeValueAsString(moveRequest);

        mockMvc.perform(post("/actions/move-troops").contentType(MediaType.APPLICATION_JSON).content(request).session(session)).andExpect(status().isForbidden());
    }

    @Test
    public void moveTroops_returnsForbidden_whenPlayerDoesNotOwnEndingTerritory() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 1);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        PlayerTerritory playerTerritoryOne = PlayerTerritory.builder().id(1L).playerId(1L).territoryId(7L).troops(10).territory(Territory.builder().south(9L).build()).build();
        PlayerTerritory playerTerritoryTwo = PlayerTerritory.builder().id(2L).playerId(1L).territoryId(9L).troops(2).territory(Territory.builder().north(7L).build()).build();
        Player player = Player.builder()
                .playerNumber(1)
                .playerTerritoriesList(Arrays.asList(playerTerritoryOne, playerTerritoryTwo))
                .remainingActions(2)
                .build();

        Game game = Game.builder()
                .players(Arrays.asList(player))
                .turnNumber(1)
                .build();

        when(mockGameRepository.findOne("gamename")).thenReturn(game);

        ObjectMapper objectMapper = new ObjectMapper();
        ActionsController.MoveRequest moveRequest = ActionsController.MoveRequest.builder()
                .moveFrom(7L)
                .moveTo(10L)
                .numberOfTroops(5)
                .build();
        String request = objectMapper.writeValueAsString(moveRequest);

        mockMvc.perform(post("/actions/move-troops").contentType(MediaType.APPLICATION_JSON).content(request).session(session)).andExpect(status().isForbidden());
    }

    @Test
    public void moveTroops_returnsForbidden_whenTerritoriesDoNotNeighborEachother() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 1);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        PlayerTerritory playerTerritoryOne = PlayerTerritory.builder().id(1L).playerId(1L).territoryId(7L).troops(10).territory(Territory.builder().build()).build();
        PlayerTerritory playerTerritoryTwo = PlayerTerritory.builder().id(2L).playerId(1L).territoryId(9L).troops(2).territory(Territory.builder().build()).build();
        Player player = Player.builder()
                .playerNumber(1)
                .playerTerritoriesList(Arrays.asList(playerTerritoryOne, playerTerritoryTwo))
                .remainingActions(2)
                .build();

        Game game = Game.builder()
                .players(Arrays.asList(player))
                .turnNumber(1)
                .build();

        when(mockGameRepository.findOne("gamename")).thenReturn(game);

        ObjectMapper objectMapper = new ObjectMapper();
        ActionsController.MoveRequest moveRequest = ActionsController.MoveRequest.builder()
                .moveFrom(7L)
                .moveTo(9L)
                .numberOfTroops(5)
                .build();
        String request = objectMapper.writeValueAsString(moveRequest);

        mockMvc.perform(post("/actions/move-troops").contentType(MediaType.APPLICATION_JSON).content(request).session(session)).andExpect(status().isForbidden());

    }


    @Test
    public void moveTroops_returnsForbidden_whenWouldMoveOutAllOrMoreTroops() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_PLAYER_NUMBER_FIELD, 1);
        session.setAttribute(SESSION_GAME_NAME_FIELD, "gamename");

        PlayerTerritory playerTerritoryOne = PlayerTerritory.builder().id(1L).playerId(1L).territoryId(7L).troops(10).territory(Territory.builder().south(9L).build()).build();
        PlayerTerritory playerTerritoryTwo = PlayerTerritory.builder().id(2L).playerId(1L).territoryId(9L).troops(2).territory(Territory.builder().north(7L).build()).build();
        Player player = Player.builder()
                .playerNumber(1)
                .playerTerritoriesList(Arrays.asList(playerTerritoryOne, playerTerritoryTwo))
                .remainingActions(2)
                .build();

        Game game = Game.builder()
                .players(Arrays.asList(player))
                .turnNumber(1)
                .build();

        when(mockGameRepository.findOne("gamename")).thenReturn(game);

        ObjectMapper objectMapper = new ObjectMapper();
        ActionsController.MoveRequest moveRequest = ActionsController.MoveRequest.builder()
                .moveFrom(7L)
                .moveTo(9L)
                .numberOfTroops(10)
                .build();
        String request = objectMapper.writeValueAsString(moveRequest);

        mockMvc.perform(post("/actions/move-troops").contentType(MediaType.APPLICATION_JSON).content(request).session(session)).andExpect(status().isForbidden());
    }
}