package org.cost.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cost.game.Game;
import org.cost.game.GameRepository;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    private GameRepository mockGameRepository;

    @Before
    public void setup() {
        mockRepository = mock(PlayerRepository.class);
        mockPlayerNumberService = mock(PlayerNumberService.class);
        mockGameRepository = mock(GameRepository.class);
        playerController = new PlayerController(mockRepository, mockPlayerNumberService, mockGameRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(playerController).build();
    }

    @Test
    public void createPlayer_addsPlayerToGame() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player[]{null}));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);
        when(mockGameRepository.findOne(anyString())).thenReturn(new Game());
        when(mockPlayerNumberService.getNumberOfPlayersInGame(players)).thenReturn(66);
        when(mockPlayerNumberService.getNextPlayerNumber(66)).thenReturn(999);

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()))
                .andExpect(status().isOk());

        Player expectedPlayer = Player.builder().name("zxmbies").gameName("Excalibur").playerNumber(999).build();
        verify(mockRepository).save(expectedPlayer);
    }

    @Test
    public void getPlayer_savesGameToHttpSession() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player[]{null}));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);
        when(mockGameRepository.findOne(anyString())).thenReturn(new Game());

        MockHttpSession httpSession = new MockHttpSession();
        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()).session(httpSession));

        assertThat(httpSession.getAttribute("game_name")).isEqualTo("Excalibur");
    }

    @Test
    public void createPlayer_returnsConflict_whenFourOrMorePlayersInGame() throws Exception {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player(), new Player(), new Player(), new Player()));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);
        when(mockGameRepository.findOne(anyString())).thenReturn(new Game());

        String contentAsString = mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()))
                .andExpect(status().isConflict()).andReturn().getResponse().getContentAsString();

        assertThat(contentAsString).isEqualTo("Game Lobby is Full");
    }

    @Test
    public void createPlayer_returnsConflict_whenPlayerAlreadyInGame() throws Exception {
        Player player = Player.builder().name("zxmbies").build();
        ArrayList<Player> players = new ArrayList<>(Collections.singletonList(player));
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(players);
        when(mockGameRepository.findOne(anyString())).thenReturn(new Game());

        String contentAsString = mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()))
                .andExpect(status().isConflict()).andReturn().getResponse().getContentAsString();

        assertThat(contentAsString).isEqualTo("Player name already taken");
    }

    @Test
    public void createPlayer_returnsResourceNotFound_whenGameDoesNotExist() throws Exception {
        when(mockGameRepository.findOne(anyString())).thenReturn(null);

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
        List<Player> players = Arrays.asList(
            Player.builder().playerNumber(1).name("zxmbies").build(),
            Player.builder().playerNumber(2).name("qxc").build(),
            Player.builder().playerNumber(3).name("eidlyn").build());
        when(mockRepository.findPlayersByGameName("gamename")).thenReturn(players);

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        String actualResponse = mockMvc.perform(get("/players/3").contentType(MediaType.APPLICATION_JSON).session(mockHttpSession))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();


        JSONAssert.assertEquals("{\n" +
                "  \"playerNumber\": 3,\n" +
                "  \"name\": \"eidlyn\"\n" +
                "}", actualResponse, JSONCompareMode.LENIENT);
    }

    @Test
    public void getPlayer_returns404WhenPlayerNumberNotFoundInGame() throws Exception {
        List<Player> players = Collections.singletonList(Player.builder().playerNumber(1).name("zxmbies").build());
        when(mockRepository.findPlayersByGameName("gamename")).thenReturn(players);

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        mockMvc.perform(get("/players/6").contentType(MediaType.APPLICATION_JSON).session(mockHttpSession))
                .andExpect(status().isNotFound());

    }

    @Test
    public void getPlayers_returnsListOfPlayersInGame() throws Exception {
        List<Player> players = Arrays.asList(
                Player.builder().playerNumber(1).name("zxmbies").build(),
                Player.builder().playerNumber(2).name("qxc").build(),
                Player.builder().playerNumber(3).name("eidlyn").build());
        when(mockRepository.findPlayersByGameName("gamename")).thenReturn(players);

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        String actualResponse = mockMvc.perform(get("/players").contentType(MediaType.APPLICATION_JSON).session(mockHttpSession))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals("[\n" +
                "  {\"name\": \"zxmbies\",\n" +
                "    \"playerNumber\": 1,\n" +
                "    \"links\": [\n" +
                "      {\n" +
                "        \"rel\": \"self\",\n" +
                "        \"href\": \"http://localhost/players/1\"\n" +
                "      }\n" +
                "    ]},\n" +
                "  {\"name\": \"qxc\",\n" +
                "    \"playerNumber\": 2,\n" +
                "    \"links\": [\n" +
                "      {\n" +
                "        \"rel\": \"self\",\n" +
                "        \"href\": \"http://localhost/players/2\"\n" +
                "      }\n" +
                "    ]},\n" +
                "  {\"name\": \"eidlyn\",\n" +
                "    \"playerNumber\": 3,\n" +
                "    \"links\": [\n" +
                "      {\n" +
                "        \"rel\": \"self\",\n" +
                "        \"href\": \"http://localhost/players/3\"\n" +
                "      }\n" +
                "    ]}\n" +
                "]", actualResponse, JSONCompareMode.LENIENT);

    }

    @Test
    public void getPlayers_returns404_whenNotInGame() throws Exception {
        mockMvc.perform(get("/players").contentType(MediaType.APPLICATION_JSON).session(new MockHttpSession()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void postPlayer_returnsError_whenGameStarted() throws Exception {
        Game game = Game.builder().gameName("Excalibur").started(1).build();
        when(mockRepository.findPlayersByGameName("Excalibur")).thenReturn(new ArrayList<>(
                Arrays.asList(
                        Player.builder().name("a").build(),
                        Player.builder().name("b").build(),
                        Player.builder().name("c").build())));
        when(mockGameRepository.findOne("Excalibur")).thenReturn(game);

        String contentAsString = mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()))
                .andExpect(status().isConflict()).andReturn().getResponse().getContentAsString();

        assertThat(contentAsString).isEqualTo("The game has already started.");


    }
}