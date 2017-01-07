package org.cost.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cost.Exceptions;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlayerControllerTest {
    private MockMvc mockMvc;
    private PlayerRepository mockRepository;
    private PlayerController playerController;
    private PlayerNumberService mockPlayerNumberService;
    private GameRepository mockGameRepository;
    private PlayerService mockPlayerService;

    @Before
    public void setup() {
        mockRepository = mock(PlayerRepository.class);
        mockPlayerNumberService = mock(PlayerNumberService.class);
        mockGameRepository = mock(GameRepository.class);
        mockPlayerService = mock(PlayerService.class);
        playerController = new PlayerController(mockRepository, mockPlayerNumberService, mockGameRepository, mockPlayerService);

        mockMvc = MockMvcBuilders.standaloneSetup(playerController).build();
    }

    @Test
    public void postPlayer_callsPlayerService_andReturnsOkStatus() throws Exception {
        MockHttpSession mockHttpSession = new MockHttpSession();

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()).session(mockHttpSession))
                .andExpect(status().isOk());

        verify(mockPlayerService).addPlayerToGame(CreatePlayerRequest.builder().playerName("zxmbies").gameName("Excalibur").build(), mockHttpSession);
    }

    @Test
    public void postPlayer_callsPlayerService_andThrowsException() throws Exception {
        doThrow(new Exceptions.ConflictException("AH!")).when(mockPlayerService).addPlayerToGame(any(), any());

        MockHttpSession mockHttpSession = new MockHttpSession();

        mockMvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(getPostRequestContentString()).session(mockHttpSession))
                .andExpect(status().isConflict());

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
        List<PlayerTerritory> territories = Collections.singletonList(
                PlayerTerritory.builder().gameName("gamename").territoryId(2L).troops(3).playerId(3L).territoryName("Cliffs 2").build());
        List<PlayerTerritory> territories2 = Collections.singletonList(
                PlayerTerritory.builder().gameName("gamename").territoryId(4L).troops(14).playerId(2L).territoryName("Hills 2").build());
        List<PlayerTerritory> territories3 = Collections.singletonList(
                PlayerTerritory.builder().gameName("gamename").territoryId(1L).troops(14).playerId(1L).territoryName("Hills 1").build());
        List<Player> players = Arrays.asList(
                Player.builder()
                        .gameName("gamename")
                        .name("zxmbies")
                        .playerTerritoriesList(territories)
                        .playerId(3L)
                        .playerNumber(1)
                        .build(),
                Player.builder()
                        .gameName("gamename")
                        .name("qxc")
                        .playerTerritoriesList(territories2)
                        .playerId(3L)
                        .playerNumber(2)
                        .build(),
                Player.builder()
                        .gameName("gamename")
                        .name("eidlyn")
                        .playerTerritoriesList(territories3)
                        .playerId(2L)
                        .playerNumber(3)
                        .build());
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
    public void getPlayerByPlayerNumber_returnsListOfTerritoryLinks() throws Exception {
        List<PlayerTerritory> territories = Arrays.asList(
                PlayerTerritory.builder().gameName("gamename").territoryId(1L).troops(2).playerId(3L).territoryName("Cliffs 1").build(),
                PlayerTerritory.builder().gameName("gamename").territoryId(2L).troops(3).playerId(3L).territoryName("Cliffs 2").build());
        List<Player> players = Collections.singletonList(
                Player.builder()
                        .gameName("gamename")
                        .name("player")
                        .playerTerritoriesList(territories)
                        .playerId(3L)
                        .playerNumber(1)
                        .build());
        when(mockRepository.findPlayersByGameName("gamename")).thenReturn(players);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        String JsonResponse = mockMvc.perform(get("/players/1").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        System.out.print(JsonResponse);
        JSONAssert.assertEquals(" {\"name\": \"player\",\n" +
                "    \"playerNumber\": 1,\n" +
                "    \"ownedTerritories\":[\n" +
                "      {\n" +
                "        \"name\": \"Cliffs 1\",\n" +
                "        \"links\": [{\n" +
                "          \"rel\": \"self\",\n" +
                "          \"href\": \"http://localhost/territories/1\"\n" +
                "        }]\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"Cliffs 2\",\n" +
                "        \"links\": [{\n" +
                "          \"rel\": \"self\",\n" +
                "          \"href\": \"http://localhost/territories/2\"\n" +
                "        }]\n" +
                "      }\n" +
                "    ]\n" +
                " }", JsonResponse, JSONCompareMode.LENIENT);

    }

    private String getPostRequestContentString() throws JsonProcessingException {
        CreatePlayerRequest playerRequest = CreatePlayerRequest.builder().gameName("Excalibur").playerName("zxmbies").build();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(playerRequest);
    }


    @Test
    public void getPlayersIncludesTroopsTerritoriesAndSupplyDepotsInResponse() throws Exception {
        List<PlayerTerritory> territories = Arrays.asList(
                PlayerTerritory.builder().gameName("gamename").territoryId(1L).troops(2).playerId(3L).territoryName("Cliffs 1").build(),
                PlayerTerritory.builder().gameName("gamename").territoryId(2L).troops(3).playerId(3L).territoryName("Cliffs 2").supplyDepotTerritory(true).build());
        List<PlayerTerritory> territories2 = Arrays.asList(
                PlayerTerritory.builder().gameName("gamename").territoryId(3L).troops(7).playerId(2L).territoryName("Hills 1").build(),
                PlayerTerritory.builder().gameName("gamename").territoryId(9L).troops(0).playerId(2L).territoryName("Island 4").supplyDepotTerritory(true).build(),
                PlayerTerritory.builder().gameName("gamename").territoryId(4L).troops(14).playerId(2L).territoryName("Hills 2").supplyDepotTerritory(true).build());
        List<Player> players = Arrays.asList(
                Player.builder()
                        .gameName("gamename")
                        .name("player1")
                        .playerTerritoriesList(territories)
                        .playerId(3L)
                        .playerNumber(1)
                        .build(),
                Player.builder()
                        .gameName("gamename")
                        .name("player2")
                        .playerTerritoriesList(territories2)
                        .playerId(2L)
                        .playerNumber(2)
                        .build());
        when(mockRepository.findPlayersByGameName("gamename")).thenReturn(players);

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        String actualResponse = mockMvc.perform(get("/players").contentType(MediaType.APPLICATION_JSON).session(mockHttpSession))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals("[\n" +
                "  {\"name\": \"player1\",\n" +
                "    \"troops\": 5,\n" +
                "    \"territories\": 2,\n" +
                "    \"playerNumber\": 1,\n" +
                "    \"supplyDepots\": 1,\n" +
                "    \"links\": [\n" +
                "      {\n" +
                "        \"rel\": \"self\",\n" +
                "        \"href\": \"http://localhost/players/1\"\n" +
                "      }\n" +
                "    ]},\n" +
                "  {\"name\": \"player2\",\n" +
                "    \"troops\": 21,\n" +
                "    \"playerNumber\": 2,\n" +
                "    \"territories\": 3,\n" +
                "    \"supplyDepots\": 2,\n" +
                "    \"links\": [\n" +
                "      {\n" +
                "        \"rel\": \"self\",\n" +
                "        \"href\": \"http://localhost/players/2\"\n" +
                "      }\n" +
                "    ]}\n" +
                "]", actualResponse, JSONCompareMode.LENIENT);

    }

}