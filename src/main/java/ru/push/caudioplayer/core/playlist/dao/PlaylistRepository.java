package ru.push.caudioplayer.core.playlist.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistEntity;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, String> {

	List<PlaylistEntity> findByTitle(String title);

}
