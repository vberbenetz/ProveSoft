package com.provesoft.gateway.repository;

import com.provesoft.gateway.entity.SignoffPathId;
import com.provesoft.gateway.entity.SignoffPathIdKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignoffPathIdRepository extends JpaRepository<SignoffPathId, SignoffPathIdKey> {
}
