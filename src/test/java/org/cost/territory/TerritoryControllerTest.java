package org.cost.territory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cost.SupplyRaidServerApplication;
import org.cost.game.Game;
import org.cost.player.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class TerritoryControllerTest {

    private MockMvc mockMvc;
    private TerritoryRepository mockRepository;
    private PlayerRepository mockPlayerRepository;
    private PlayerTerritoryRepository mockPlayerTerritoryRepository;

    @Before
    public void setup() {
        mockPlayerRepository = mock(PlayerRepository.class);
        mockRepository = mock(TerritoryRepository.class);
        mockPlayerTerritoryRepository = mock(PlayerTerritoryRepository.class);
        TerritoryController territoryController = new TerritoryController(
                mockRepository, mockPlayerRepository, mockPlayerTerritoryRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(territoryController).build();
    }

    @Test
    public void getTerritory_returnsTerritoryResponseObject() throws Exception {
        when(mockRepository.findOne(4L)).thenReturn(Territory.builder().name("Cliffs 1").north(null).east(5L).south(13L).west(null).build());
        when(mockRepository.findOne(5L)).thenReturn(Territory.builder().name("Cliffs 2").territoryId(5L).build());
        when(mockRepository.findOne(13L)).thenReturn(Territory.builder().name("Cliffs 4").territoryId(13L).build());
        when(mockPlayerTerritoryRepository.findPlayerTerritoryByTerritoryIdAndGameName(4L, "gamename")).thenReturn(new PlayerTerritory());

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        String response = mockMvc.perform(get("/territories/4").accept(MediaType.APPLICATION_JSON).session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals("{\n" +
                "  \"name\": \"Cliffs 1\",\n" +
                "  \"north\": null,\n" +
                "  \"south\": {\n" +
                "    \"name\": \"Cliffs 4\",\n" +
                "    \"links\": [{\n" +
                "      \"rel\": \"self\",\n" +
                "      \"href\": \"http://localhost/territories/13\"\n" +
                "    }]\n" +
                "  },\n" +
                "  \"east\": {\n" +
                "    \"name\": \"Cliffs 2\",\n" +
                "    \"links\": [{\n" +
                "      \"rel\": \"self\",\n" +
                "      \"href\": \"http://localhost/territories/5\"\n" +
                "    }]\n" +
                "  },\n" +
                "  \"west\": null\n" +
                "}", response, JSONCompareMode.LENIENT);
    }

    @Test
    public void getTerritory_returns404_whenTerritoryOutsideRange() throws Exception {
        when(mockRepository.getOne(-1L)).thenReturn(null);

        mockMvc.perform(get("/territories/-1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getOwnerofTerritory_returns_linkToOwnerOfTerritory() throws Exception {
        Player player = Player.builder().playerNumber(2).name("player").build();
        PlayerTerritory playerTerritory = PlayerTerritory.builder().playerId(30L).territoryId(1L).player(player).build();
        when(mockRepository.findOne(1L)).thenReturn((Territory.builder().name("Location 1").territoryId(1L).build()));
        when(mockPlayerTerritoryRepository.findPlayerTerritoryByTerritoryIdAndGameName(1L, "gamename"))
                .thenReturn(playerTerritory);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        String response = mockMvc.perform(get("/territories/1").accept(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals("{\n" +
                "  \"name\" : \"Location 1\",\n" +
                "  \"owningPlayer\": {\n" +
                "     \"name\": \"player\",\n" +
                "     \"playerNumber\": 2,\n" +
                "     \"links\":  [ {\n" +
                "       \"rel\": \"self\",\n" +
                "       \"href\": \"http://localhost/players/2\"\n" +
                "     }]\n" +
                "  }\n" +
                "}", response, JSONCompareMode.LENIENT);


    }

    @Test
    public void getOwnerOfTerritory_returnsNullOwningPlayer_whenNoOwner() throws Exception {
        PlayerTerritory playerTerritory = PlayerTerritory.builder().playerId(30L).territoryId(1L).player(null).build();
        when(mockRepository.findOne(1L)).thenReturn((Territory.builder().name("Location 1").territoryId(1L).build()));
        when(mockPlayerTerritoryRepository.findPlayerTerritoryByTerritoryIdAndGameName(1L, "gamename"))
                .thenReturn(playerTerritory);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        String response = mockMvc.perform(get("/territories/1").accept(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals("{\n" +
                "  \"name\" : \"Location 1\",\n" +
                "  \"owningPlayer\": null\n" +
                "}", response, JSONCompareMode.LENIENT);

    }

    @Test
    public void postTerritoryOwner_setsOwnerOfTerritory_toRequestedPlayer() throws Exception {
        PlayerTerritory playerTerritory = PlayerTerritory.builder().territoryId(2L).build();
        when(mockPlayerTerritoryRepository.findPlayerTerritoryByTerritoryIdAndGameName(2L, "gamename")).thenReturn(playerTerritory);

        Player player = Player.builder().playerNumber(1).playerTerritoriesList(new ArrayList<>()).build();
        Player wrongPlayer = Player.builder().playerNumber(4).build();
        when(mockPlayerRepository.findPlayersByGameName("gamename")).thenReturn(new ArrayList<>(Arrays.asList(player, wrongPlayer)));

        TerritoryController.TerritoryRequest territoryRequest = TerritoryController.TerritoryRequest.builder().playerNumber(1).territoryId(2).build();
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(territoryRequest);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        mockMvc.perform(post("/territories/owner").contentType(MediaType.APPLICATION_JSON).content(content).session(mockHttpSession))
                .andExpect(status().isOk());

        verify(mockPlayerRepository).save(player);

        assertThat(player.getPlayerTerritoriesList().get(0)).isSameAs(playerTerritory);
    }

    @Test
    public void postTerritoryOwner_returnsNotFound_whenTerritoryNotFound() throws Exception{
        Player player = Player.builder().playerNumber(1).playerTerritoriesList(new ArrayList<>()).build();
        Player wrongPlayer = Player.builder().playerNumber(4).build();
        when(mockPlayerRepository.findPlayersByGameName("gamename")).thenReturn(new ArrayList<>(Arrays.asList(player, wrongPlayer)));
        when(mockPlayerTerritoryRepository.findPlayerTerritoryByTerritoryIdAndGameName(2L, "gamename")).thenReturn(null);

        TerritoryController.TerritoryRequest territoryRequest = TerritoryController.TerritoryRequest.builder().playerNumber(1).territoryId(2).build();
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(territoryRequest);

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        mockMvc.perform(post("/territories/owner").contentType(MediaType.APPLICATION_JSON).content(content).session(mockHttpSession))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTerritoryById_returnsNumberOfTroopsOnTerritory() throws Exception {
        when(mockRepository.findOne(1L)).thenReturn((Territory.builder().name("Location 1").territoryId(1L).build()));
        PlayerTerritory playerTerritory = PlayerTerritory.builder().playerId(30L).territoryId(1L).
                player(Player.builder().playerNumber(2).name("player").build()).troops(3).build();
        when(mockPlayerTerritoryRepository.findPlayerTerritoryByTerritoryIdAndGameName(1L, "gamename"))
                .thenReturn(playerTerritory);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        String response = mockMvc.perform(get("/territories/1").accept(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println(response);

        JSONAssert.assertEquals("{\n" +
                "  \"name\" : \"Location 1\",\n" +
                "  \"owningPlayer\": {\n" +
                "     \"name\": \"player\",\n" +
                "     \"playerNumber\": 2,\n" +
                "     \"links\":  [ {\n" +
                "       \"rel\": \"self\",\n" +
                "       \"href\": \"http://localhost/players/2\"\n" +
                "     }]\n" +
                "  },\n" +
                "  \"troops\": 3\n" +
                "}", response, JSONCompareMode.LENIENT);

    }

    @Test
    public void getTerritories_returnsListOfTerritoriesWithLinksIDsAndSupplyDepot() throws Exception {
        when(mockPlayerTerritoryRepository.findByGameName("gamename")).thenReturn(Arrays.asList(
                PlayerTerritory.builder()
                        .territoryName("Location 1").territoryId(1L).
                        supplyDepotTerritory(false).build(),
                PlayerTerritory.builder()
                        .territoryName("Location 2").territoryId(2L).
                        supplyDepotTerritory(true).build()));


        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        String response = mockMvc.perform(get("/territories").accept(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.print(response);

        JSONAssert.assertEquals("[\n" +
                "  {\"name\": \"Location 1\",\n" +
                "    \"territoryId\": 1,\n" +
                "    \"supplyDepot\": false,\n" +
                "  \"links\": [\n" +
                "    {\n" +
                "      \"rel\": \"self\",\n" +
                "      \"href\": \"http://localhost/territories/1\"\n" +
                "    }\n" +
                "  ]},\n" +
                "  {\"name\": \"Location 2\",\n" +
                "    \"territoryId\": 2,\n" +
                "    \"supplyDepot\": true,\n" +
                "    \"links\": [\n" +
                "      {\n" +
                "        \"rel\": \"self\",\n" +
                "        \"href\": \"http://localhost/territories/2\"\n" +
                "      }\n" +
                "    ]}\n" +
                "]", response, JSONCompareMode.STRICT);
    }
}