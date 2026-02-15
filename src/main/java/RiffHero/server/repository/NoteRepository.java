package RiffHero.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import RiffHero.client.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {}
