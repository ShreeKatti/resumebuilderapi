package com.shreek.resumebuilderapi.repository;

import com.shreek.resumebuilderapi.document.resume;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResumeRepository extends MongoRepository<resume, String> {


}
