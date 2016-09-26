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

/**
 * Created by Jordon on 9/19/2016.
 */
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
        Game excalibur = new Game();
        excalibur.setGameName("Excalibur");
        gameRepository.save(excalibur);

        Player player = new Player();
        player.setName("zxmbies");
        player.setGameName("Excalibur");
        playerRepository.save(player);

        gameRepository.delete("Excalibur");
    }

    @After
    public void teardown() {
        gameRepository.deleteAll();
        playerRepository.deleteAll();
    }
}
