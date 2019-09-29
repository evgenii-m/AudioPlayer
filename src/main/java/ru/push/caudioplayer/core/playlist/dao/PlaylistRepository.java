package ru.push.caudioplayer.core.playlist.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistEntity;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, String> {

}
