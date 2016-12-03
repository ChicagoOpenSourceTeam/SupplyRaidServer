package org.cost.game;

import org.cost.player.Player;
import org.cost.player.PlayerRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GameRepositoryTest {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Test
    public void deleteGame_deletesPlayers_whenPlayersExist() {
        Game excalibur = Game.builder().gameName("Excalibur").build();
        gameRepository.save(excalibur);

        Player player = Player.builder().name("zxmbies").gameName("Excalibur").build();
        playerRepository.save(player);

        gameRepository.delete("Excalibur");
    }

    @After
    public void teardown() {
        gameRepository.deleteAll();
        playerRepository.deleteAll();
    }
}
