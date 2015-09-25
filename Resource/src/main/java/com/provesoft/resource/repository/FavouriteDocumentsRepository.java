package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.FavouriteDocumentKey;
import com.provesoft.resource.entity.Document.FavouriteDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FavouriteDocumentsRepository extends JpaRepository<FavouriteDocuments, FavouriteDocumentKey> {

    List<FavouriteDocuments> findByKeyCompanyNameAndKeyEmail(String companyName, String email);

    @Query(
            "DELETE FROM FavouriteDocuments fd " +
            "WHERE fd.key.companyName=:companyName " +
            "AND fd.key.email=:email " +
            "AND fd.key.documentId=:documentId"
    )
    @Modifying
    @Transactional
    void removeFavouriteDocument(@Param("companyName") String companyName,
                                 @Param("email") String email,
                                 @Param("documentId") String documentId);
}
