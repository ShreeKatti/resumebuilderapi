package com.shreek.resumebuilderapi.repository;

import com.shreek.resumebuilderapi.document.resume;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends MongoRepository<resume, String> {

    List<resume> findByUserIdOrderByUpdatedAtDesc(String userId);

    Optional<resume> findByUserIdAndId(String id, String resumeId);
}
