package RiffHero.server.service;

import org.springframework.stereotype.Service;

import RiffHero.server.repository.SongRepository;

@Service
public class SongService {

  private final SongRepository songRepository;

  public SongService(SongRepository songRepository) {
    this.songRepository = songRepository;
  }

}
