package org.cost.board;

import org.cost.player.*;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BoardControllerTest {

    private PlayerTerritoryRepository mockPlayerTerritoryRepository;
    private PlayerRepository mockPlayerRepository;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockPlayerTerritoryRepository = mock(PlayerTerritoryRepository.class);
        mockPlayerRepository = mock(PlayerRepository.class);
        BoardController boardController = new BoardController(mockPlayerTerritoryRepository, mockPlayerRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(boardController).build();
    }

    @Test
    public void getBoard_returnsBoardDetails() throws Exception {
        List<PlayerTerritory> territories = Arrays.asList(
                PlayerTerritory.builder().gameName("gamename").territoryId(1L).troops(2).playerId(3L).territoryName("Cliffs 1").build(),
                PlayerTerritory.builder().gameName("gamename").territoryId(2L).troops(3).playerId(3L).territoryName("Cliffs 2").supplyDepotTerritory(true).build());
        List<PlayerTerritory> territories2 = Arrays.asList(
                PlayerTerritory.builder().gameName("gamename").territoryId(3L).troops(7).playerId(2L).territoryName("Hills 1").build(),
                PlayerTerritory.builder().gameName("gamename").territoryId(9L).troops(0).playerId(2L).territoryName("Island 4").supplyDepotTerritory(true).build(),
                PlayerTerritory.builder().gameName("gamename").territoryId(4L).troops(14).playerId(2L).territoryName("Hills 2").supplyDepotTerritory(true).build());
        Player player1 = Player.builder()
                .gameName("gamename")
                .name("player1")
                .playerTerritoriesList(territories)
                .playerId(3L)
                .playerNumber(1)
                .build();
        Player player2 = Player.builder()
                .gameName("gamename")
                .name("player2")
                .playerTerritoriesList(territories2)
                .playerId(2L)
                .playerNumber(2)
                .build();
        List<Player> players = Arrays.asList(
                player1,
                player2);

        when(mockPlayerRepository.findPlayersByGameName("gamename")).thenReturn(players);

        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(Arrays.asList(
                PlayerTerritory.builder()
                        .territoryName("Location 1").territoryId(1L).troops(11).player(player1)
                        .supplyDepotTerritory(false).supplied(false).build(),
                PlayerTerritory.builder()
                        .territoryName("Location 2").territoryId(2L).troops(3).player(player2)
                        .supplyDepotTerritory(true).supplied(true).build()));


        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");

        String actualResponse = mockMvc.perform(get("/board").contentType(MediaType.APPLICATION_JSON).session(mockHttpSession))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();


        JSONAssert.assertEquals("{\n" +
                "  \"territories\": [\n" +
                "    {\n" +
                "      \"name\": \"Location 1\",\n" +
                "      \"territoryId\": 1,\n" +
                "      \"supplyDepot\": false,\n" +
                "      \"supplied\": false,\n" +
                "      \"troops\": 11,\n" +
                "      \"playerNumber\": 1,\n" +
                "      \"links\": [\n" +
                "        {\n" +
                "          \"rel\": \"self\",\n" +
                "          \"href\": \"http://localhost/territories/1\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Location 2\",\n" +
                "      \"territoryId\": 2,\n" +
                "      \"supplyDepot\": true,\n" +
                "      \"supplied\": true,\n" +
                "      \"troops\": 3,\n" +
                "      \"playerNumber\": 2,\n" +
                "      \"links\": [\n" +
                "        {\n" +
                "          \"rel\": \"self\",\n" +
                "          \"href\": \"http://localhost/territories/2\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"players\": [\n" +
                "    {\n" +
                "      \"name\": \"player1\",\n" +
                "      \"troops\": 5,\n" +
                "      \"territories\": 2,\n" +
                "      \"playerNumber\": 1,\n" +
                "      \"supplyDepots\": 1,\n" +
                "      \"links\": [\n" +
                "        {\n" +
                "          \"rel\": \"self\",\n" +
                "          \"href\": \"http://localhost/players/1\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"player2\",\n" +
                "      \"troops\": 21,\n" +
                "      \"playerNumber\": 2,\n" +
                "      \"territories\": 3,\n" +
                "      \"supplyDepots\": 2,\n" +
                "      \"links\": [\n" +
                "        {\n" +
                "          \"rel\": \"self\",\n" +
                "          \"href\": \"http://localhost/players/2\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}", actualResponse, JSONCompareMode.LENIENT);
    }
}