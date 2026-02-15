package RiffHero.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import RiffHero.server.model.Song;

public interface SongRepository extends JpaRepository<Song, Long> {}
