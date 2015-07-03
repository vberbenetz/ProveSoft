package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, String> {

}
