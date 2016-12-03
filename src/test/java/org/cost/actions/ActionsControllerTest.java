package org.cost.actions;

import org.cost.game.Game;
import org.cost.game.GameRepository;
import org.cost.player.Player;
import org.cost.player.PlayerController;
import org.cost.player.PlayerRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cost.player.PlayerController.SESSION_GAME_NAME_FIELD;
import static org.cost.player.PlayerController.SESSION_PLAYER_NUMBER_FIELD;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ActionsControllerTest {

    private MockMvc mockMvc;
    private GameRepository mockGameRepository;
    private PlayerRepository mockPlayerRepository;

    @Before
    public void setup() {
        mockGameRepository = mock(GameRepository.class);
        mockPlayerRepository = mock(PlayerRepository.class);
        ActionsController actionsController = new ActionsController(mockGameRepository, mockPlayerRepository);
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

        mockMvc.perform(post("/actions/skip-action").contentType(MediaType.APPLICATION_JSON).session(session)).andExpect(status().isOk());

        verify(mockPlayerRepository).save(thisPlayer);
        assertThat(thisPlayer.getRemainingActions()).isEqualTo(2);
    }

    @Test
    public void skip_returnsForbiddenStatus_whenPlayerIsNotActivePlayer() throws Exception {
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

        verifyZeroInteractions(mockPlayerRepository);
    }
}