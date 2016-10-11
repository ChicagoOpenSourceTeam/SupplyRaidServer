package org.cost.player;



import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cost.territory.Territory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SuppliedStatusServiceTest {

    private PlayerRepository mockPlayerRepository;
    private SuppliedStatusService suppliedStatusService;
    private PlayerTerritoryRepository mockPlayerTerritoryRepository;

    @Before
    public void setup() {
        mockPlayerRepository = mock(PlayerRepository.class);
        mockPlayerTerritoryRepository = mock(PlayerTerritoryRepository.class);
        suppliedStatusService = new SuppliedStatusService(mockPlayerRepository, mockPlayerTerritoryRepository);
    }

    @Test
    public void markUnsupplied_setsAllSuppliedOwnedTerritories_toUnsupplied() {
        List<Player> players = Arrays.asList(
                Player.builder().playerTerritoriesList(
                        Arrays.asList(
                                PlayerTerritory.builder().supplied(true).build(),
                                PlayerTerritory.builder().supplied(false).build()
                        )
                ).build(),
                Player.builder().playerTerritoriesList(
                        Arrays.asList(
                                PlayerTerritory.builder().supplied(true).build()
                        )
                ).build());
        when(mockPlayerRepository.findAll()).thenReturn(players);

        suppliedStatusService.markUnsupplied();

        verify(mockPlayerRepository).save(players);
        assertTrue(players
                .stream()
                .allMatch(p -> p.getPlayerTerritoriesList()
                        .stream()
                        .allMatch(pt -> !pt.isSupplied())
                )
        );
        assertThat(players
                .stream()
                .mapToInt(p -> p.getPlayerTerritoriesList().size())
                .sum()
        ).isEqualTo(3);
    }

    @Test
    public void markSupplied_suppliesTerritories() {

        /*
         *   P2  | P1  | UO  | P1
         *   UO  | P1S | P1  | P1
         *         P2  | P2  | P1
         *               P2  | P2S
         */

        PlayerTerritory supplyDepot1 = PlayerTerritory
                .builder()
                .territoryName("Islands 1")
                .territoryId(6L)
                .player(Player.builder().playerNumber(1).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(7L)
                        .north(2L)
                        .west(5L)
                        .south(9L)
                        .build())
                .build();
        PlayerTerritory supplyDepot2 = PlayerTerritory
                .builder()
                .territoryName("Seas 3")
                .territoryId(13L)
                .player(Player.builder().playerNumber(2).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(null)
                        .north(11L)
                        .west(12L)
                        .south(null)
                        .build())
                .build();


        List<PlayerTerritory> supplyDepots = Arrays.asList(
                supplyDepot1,
                supplyDepot2);

        PlayerTerritory unsupplied = PlayerTerritory
                .builder()
                .territoryName("Hills 1")
                .territoryId(1L)
                .player(Player.builder().playerNumber(2).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(2L)
                        .north(null)
                        .west(null)
                        .south(5L)
                        .build())
                .build();
        PlayerTerritory player1supplied1 = PlayerTerritory
                .builder()
                .territoryName("Hills 2")
                .territoryId(2L)
                .player(Player.builder().playerNumber(1).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(3L)
                        .north(null)
                        .west(1L)
                        .south(6L)
                        .build())
                .build();
        PlayerTerritory unowned1 = PlayerTerritory
                .builder()
                .territoryName("Hills 3")
                .territoryId(3L)
                .player(null)
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(4L)
                        .north(null)
                        .west(2L)
                        .south(7L)
                        .build())
                .build();
        PlayerTerritory player1supplied2 = PlayerTerritory
                .builder()
                .territoryName("Cliffs 1")
                .territoryId(4L)
                .player(Player.builder().playerNumber(1).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(null)
                        .north(null)
                        .west(3L)
                        .south(8L)
                        .build())
                .build();
        PlayerTerritory unowned2 = PlayerTerritory
                .builder()
                .territoryName("Cliffs 2")
                .territoryId(5L)
                .player(null)
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(6L)
                        .north(1L)
                        .west(null)
                        .south(null)
                        .build())
                .build();
        PlayerTerritory player1supplied3 = PlayerTerritory
                .builder()
                .territoryName("Islands 2")
                .territoryId(7L)
                .player(Player.builder().playerNumber(1).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(8L)
                        .north(3L)
                        .west(6L)
                        .south(10L)
                        .build())
                .build();
        PlayerTerritory player1supplied4 = PlayerTerritory
                .builder()
                .territoryName("Islands 3")
                .territoryId(8L)
                .player(Player.builder().playerNumber(1).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(null)
                        .north(4L)
                        .west(7L)
                        .south(11L)
                        .build())
                .build();
        PlayerTerritory player2Supplied1 = PlayerTerritory
                .builder()
                .territoryName("Desert 1")
                .territoryId(9L)
                .player(Player.builder().playerNumber(2).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(10L)
                        .north(6L)
                        .west(null)
                        .south(null)
                        .build())
                .build();
        PlayerTerritory player2Supplied2 = PlayerTerritory
                .builder()
                .territoryName("Desert 2")
                .territoryId(10L)
                .player(Player.builder().playerNumber(2).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(11L)
                        .north(7L)
                        .west(9L)
                        .south(12L)
                        .build())
                .build();
        PlayerTerritory player1supplied5 = PlayerTerritory
                .builder()
                .territoryName("Seas 1")
                .territoryId(11L)
                .player(Player.builder().playerNumber(1).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(null)
                        .north(8L)
                        .west(10L)
                        .south(13L)
                        .build())
                .build();
        PlayerTerritory player2Supplied3 = PlayerTerritory
                .builder()
                .territoryName("Seas 2")
                .territoryId(12L)
                .player(Player.builder().playerNumber(2).build())
                .supplied(false)
                .territory(Territory
                        .builder()
                        .east(13L)
                        .north(10L)
                        .west(null)
                        .south(null)
                        .build())
                .build();
        List<PlayerTerritory> allPlayerTerritories = Arrays.asList(
                unsupplied,
                player1supplied1,
                unowned1,
                player1supplied2,
                unowned2,
                supplyDepot1,
                player1supplied3,
                player1supplied4,
                player2Supplied1,
                player2Supplied2,
                player1supplied5,
                player2Supplied3,
                supplyDepot2);

        suppliedStatusService.markSupplied(supplyDepots, allPlayerTerritories);

        verify(mockPlayerTerritoryRepository).save(allPlayerTerritories);

        assertThat(allPlayerTerritories
                .stream()
                .filter(PlayerTerritory::isSupplied)
                .collect(Collectors.toList())).containsExactlyInAnyOrder(
                player1supplied1,
                player1supplied2,
                supplyDepot1,
                player1supplied3,
                player1supplied4,
                player2Supplied1,
                player2Supplied2,
                player1supplied5,
                player2Supplied3,
                supplyDepot2);
    }

}