package org.cost.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cost.game.Game;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlayerControllerTest {
    private MockMvc mockMvc;
    private PlayerRepository mockRepository;

    @Before
    public void setup() {
        mockRepository = mock(PlayerRepository.class);
        PlayerController playerController = new PlayerController(mockRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(playerController).build();
    }

    @Test
    public void createPlayer_addsPlayerToGame() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player[]{null}));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getRequestContentString()))
                .andExpect(status().isOk());

        Player expectedPlayer = new Player();
        expectedPlayer.setPlayerName("zxmbies");
        expectedPlayer.setGameName("Excalibur");
        verify(mockRepository).save(expectedPlayer);
    }

    @Test
    public void createPlayer_returnsConflict_whenFourOrMorePlayersInGame() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player(), new Player(), new Player(), new Player()));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getRequestContentString()))
                .andExpect(status().isConflict());
    }

    @Test
    public void createPlayer_returnsConflict_whenPlayerAlreadyInGame() throws Exception {
        Player player = new Player();
        player.setPlayerName("zxmbies");
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getRequestContentString()))
                .andExpect(status().isConflict());
    }

    @Test
    public void createPlayer_returnsResourceNotFound_whenGameDoesNotExist() throws Exception {
        ArrayList<Player> players = new ArrayList<>();
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getRequestContentString()))
                .andExpect(status().isNotFound());
    }

    private String getRequestContentString() throws JsonProcessingException {
        PlayerController.CreatePlayerRequest playerRequest = PlayerController.CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(playerRequest);
    }
}