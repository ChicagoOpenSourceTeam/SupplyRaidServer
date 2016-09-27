package org.cost.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cost.player.Player;
import org.cost.player.PlayerController;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GameControllerTest {

    private MockMvc mockMvc;
    private GameRepository mockRepository;

    @Before
    public void setup() {
        mockRepository = mock(GameRepository.class);
        GameController gameController = new GameController(mockRepository);
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
        game.setStarted(true);
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
        game.setStarted(false);
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



}