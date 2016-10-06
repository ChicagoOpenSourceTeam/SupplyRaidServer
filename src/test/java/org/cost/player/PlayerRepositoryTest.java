package org.cost.player;

import org.cost.game.Game;
import org.cost.game.GameRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PlayerRepositoryTest {
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    GameRepository gameRepository;

    @Test
    public void findPlayersByGameName_whenNoGameExists() {
        List<Player> players = playerRepository.findPlayersByGameName("Excalibur");

        assertThat(players).hasSize(0);
    }

    @Test
    public void findPlayersByGameName_whenNoPlayersExist() {
        createGameNamedExcalibur();

        List<Player> players = playerRepository.findPlayersByGameName("Excalibur");

        assertThat(players).hasSize(1);
        assertThat(players.get(0)).isNull();
    }

    @Test
    public void findPlayersByGameName_whenPlayersExistInGame() {
        createGameNamedExcalibur();
        createPlayerZxmbiesInExcalibur();

        List<Player> players = playerRepository.findPlayersByGameName("Excalibur");

        assertThat(players.get(0).getGameName()).isEqualTo("Excalibur");
        assertThat(players.get(0).getName()).isEqualTo("zxmbies");
    }

    private void createPlayerZxmbiesInExcalibur() {
        Player player = Player.builder().name("zxmbies").gameName("Excalibur").build();
        playerRepository.save(player);
    }

    private void createGameNamedExcalibur() {
        Game excalibur = Game.builder().gameName("Excalibur").build();
        gameRepository.save(excalibur);
    }

    @After
    public void teardown() {
        playerRepository.deleteAll();
        gameRepository.deleteAll();
    }
}