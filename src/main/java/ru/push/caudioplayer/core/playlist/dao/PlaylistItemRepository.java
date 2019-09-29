package ru.push.caudioplayer.core.playlist.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistItemEntity;

@Repository
public interface PlaylistItemRepository extends JpaRepository<PlaylistItemEntity, String> {
}
