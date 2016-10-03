package org.cost.territory;

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

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        String response = mockMvc.perform(get("/territories/4").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("response = " + response);

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
    public void getTerritories_returnsListOfTerritoriesWithLinks() throws Exception {
        when(mockRepository.findAll()).thenReturn(Arrays.asList(Territory.builder().name("Location 1").territoryId(1L).build(),
                Territory.builder().name("Location 2").territoryId(2L).build()));

        String response = mockMvc.perform(get("/territories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("response = " + response);

        JSONAssert.assertEquals("[\n" +
                "  {\"name\": \"Location 1\",\n" +
                "  \"links\": [\n" +
                "    {\n" +
                "      \"rel\": \"self\",\n" +
                "      \"href\": \"http://localhost/territories/1\"\n" +
                "    }\n" +
                "  ]},\n" +
                "  {\"name\": \"Location 2\",\n" +
                "    \"links\": [\n" +
                "      {\n" +
                "        \"rel\": \"self\",\n" +
                "        \"href\": \"http://localhost/territories/2\"\n" +
                "      }\n" +
                "    ]}\n" +
                "]", response, JSONCompareMode.LENIENT);
    }

    @Test
    public void getOwnerofTerritory_returns_linkToOwnerOfTerritory() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        when(mockRepository.findOne(1L)).thenReturn((Territory.builder().name("Location 1").territoryId(1L).build()));
        PlayerTerritory playerTerritory = new PlayerTerritory();
        playerTerritory.setPlayerId(30L);
        playerTerritory.setTerritoryId(1L);
        playerTerritory.setPlayer(Player.builder().playerNumber(2).name("player").build());
        when(mockPlayerTerritoryRepository.findPlayerTerritoryByTerritoryIdAndGameName(1L, "gamename"))
                .thenReturn(playerTerritory);


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
    public void getOwnerofTerritory_returnsNullOwningPlayer_whenNoOwner() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PlayerController.SESSION_GAME_NAME_FIELD, "gamename");
        when(mockRepository.findOne(1L)).thenReturn((Territory.builder().name("Location 1").territoryId(1L).build()));
        PlayerTerritory playerTerritory = new PlayerTerritory();
        playerTerritory.setPlayerId(30L);
        playerTerritory.setTerritoryId(1L);
        playerTerritory.setPlayer(null);
        when(mockPlayerTerritoryRepository.findPlayerTerritoryByTerritoryIdAndGameName(1L, "gamename"))
                .thenReturn(playerTerritory);

        String response = mockMvc.perform(get("/territories/1").accept(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals("{\n" +
                "  \"name\" : \"Location 1\",\n" +
                "  \"owningPlayer\": null\n" +
                "}", response, JSONCompareMode.LENIENT);

    }
}