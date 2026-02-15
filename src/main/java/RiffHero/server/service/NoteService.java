package RiffHero.server.service;

import org.springframework.stereotype.Service;

import RiffHero.server.repository.NoteRepository;

@Service
public class NoteService {

  private final NoteRepository noteRepository;

  public NoteService(NoteRepository noteRepository) {
    this.noteRepository = noteRepository;
  }

}
