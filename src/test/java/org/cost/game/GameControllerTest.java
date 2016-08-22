package org.cost.game;

import ch.qos.logback.core.property.ResourceExistsPropertyDefiner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void createGame_returnsSuccess_whenGameDoesNotExist() throws Exception {
        when(mockRepository.exists("Game Name")).thenReturn(false);
        ObjectMapper objectMapper = new ObjectMapper();
        GameController.CreateGameRequest createGameRequest = GameController.CreateGameRequest.builder().gameName("Game Name").build();
        String content = objectMapper.writeValueAsString(createGameRequest);

        mockMvc.perform(post("/game").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isOk());

        Game game = new Game();
        game.setGameName("Game Name");
        verify(mockRepository).save(game);
    }

    @Test
    public void createGame_returnsFailure_whenGameAlreadyExists() throws Exception {
        when(mockRepository.exists("Game Name")).thenReturn(true);
        ObjectMapper objectMapper = new ObjectMapper();
        GameController.CreateGameRequest createGameRequest = GameController.CreateGameRequest.builder().gameName("Game Name").build();
        String content = objectMapper.writeValueAsString(createGameRequest);

        mockMvc.perform(post("/game").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isConflict());

        verify(mockRepository, times(0)).save(any(Game.class));
    }
}