package org.cost.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlayerControllerTest {
    private MockMvc mockMvc;
    private PlayerRepository mockRepository;
    private PlayerController playerController;
    private PlayerNumberService mockPlayerNumberService;

    @Before
    public void setup() {
        mockRepository = mock(PlayerRepository.class);
        mockPlayerNumberService = mock(PlayerNumberService.class);
        playerController = new PlayerController(mockRepository, mockPlayerNumberService);

        mockMvc = MockMvcBuilders.standaloneSetup(playerController).build();
    }

    @Test
    public void createPlayer_addsPlayerToGame() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player[]{null}));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);
        when(mockPlayerNumberService.getNumberOfPlayersInGame(players)).thenReturn(66);
        when(mockPlayerNumberService.getNextPlayerNumber(66)).thenReturn(999);

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()))
                .andExpect(status().isOk());

        Player expectedPlayer = new Player();
        expectedPlayer.setName("zxmbies");
        expectedPlayer.setGameName("Excalibur");
        expectedPlayer.setPlayerNumber(999);
        verify(mockRepository).save(expectedPlayer);
    }

    @Test
    public void getPlayer_savesGameToHttpSession() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player[]{null}));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);
        MockHttpSession httpSession = new MockHttpSession();

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()).session(httpSession));

        assertThat(httpSession.getAttribute("game_name")).isEqualTo("Excalibur");
    }

    @Test
    public void createPlayer_returnsConflict_whenFourOrMorePlayersInGame() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player(), new Player(), new Player(), new Player()));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()))
                .andExpect(status().isConflict());
    }

    @Test
    public void createPlayer_returnsConflict_whenPlayerAlreadyInGame() throws Exception {
        Player player = new Player();
        player.setName("zxmbies");
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(player));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()))
                .andExpect(status().isConflict());
    }

    @Test
    public void createPlayer_returnsResourceNotFound_whenGameDoesNotExist() throws Exception {
        ArrayList<Player> players = new ArrayList<>();
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()))
                .andExpect(status().isNotFound());
    }

    private String getPostRequestContentString() throws JsonProcessingException {
        PlayerController.CreatePlayerRequest playerRequest = PlayerController.CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(playerRequest);
    }

    @Test
    public void getPlayer_returnsPlayerInGameWhosePlayerNumberMatchesRequest() throws Exception {
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        ArrayList<Player> players = new ArrayList<>();
        players.add(Player.builder().playerNumber(1).name("zxmbies").build());
        players.add(Player.builder().playerNumber(2).name("qxc").build());
        players.add(Player.builder().playerNumber(3).name("eidlyn").build());
        when(mockRepository.findPlayersByGameName("gamename")).thenReturn(players);

        String actualResponse = mockMvc.perform(get("/players/3").contentType(MediaType.APPLICATION_JSON).session(mockHttpSession))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();


        JSONAssert.assertEquals("{\n" +
                "  \"playerNumber\": 3,\n" +
                "  \"name\": \"eidlyn\"\n" +
                "}", actualResponse, JSONCompareMode.LENIENT);
    }

    @Test
    public void getPlayer_returns404WhenPlayerNumberNotFoundInGame() throws Exception {
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        ArrayList<Player> players = new ArrayList<>();
        players.add(Player.builder().playerNumber(1).name("zxmbies").build());
        when(mockRepository.findPlayersByGameName("gamename")).thenReturn(players);

        mockMvc.perform(get("/players/6").contentType(MediaType.APPLICATION_JSON).session(mockHttpSession))
                .andExpect(status().isNotFound());

    }
}