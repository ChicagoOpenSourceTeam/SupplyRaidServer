package org.cost.territory;

import org.cost.SupplyRaidServerApplication;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

    @Before
    public void setup() {
        TerritoryRepository mockRepository = mock(TerritoryRepository.class);
        TerritoryController territoryController = new TerritoryController(mockRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(territoryController).build();

        when(mockRepository.findOne(anyLong())).thenReturn(Territory.builder().name("Rustic Location").build());
    }

    @Test
    public void getTerritory_returnsTerritoryObject() throws Exception {
        String response = mockMvc.perform(get("/territories/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals("{\n" +
                "  \"name\": \"Rustic Location\"\n" +
                "}", response, JSONCompareMode.LENIENT);
    }
}