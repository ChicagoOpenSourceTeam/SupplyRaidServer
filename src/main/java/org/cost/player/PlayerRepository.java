package org.cost.player;

import org.hibernate.annotations.WhereJoinTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.JoinTable;
import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    @Query(value = "SELECT PLAYER.name as name, PLAYER.player_number as player_number, PLAYER.player_id as player_id, PLAYER.remaining_actions as remaining_actions, GAME.game_name as game_name FROM GAME LEFT JOIN PLAYER ON GAME.game_name = PLAYER.game_name WHERE GAME.game_name=:gameName", nativeQuery = true)
    List<Player> findPlayersByGameName(@Param("gameName") String gameName);
}
