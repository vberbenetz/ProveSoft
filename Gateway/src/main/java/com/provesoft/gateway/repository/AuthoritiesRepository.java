package com.provesoft.gateway.repository;

import com.provesoft.gateway.entity.Authorities;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthoritiesRepository extends JpaRepository<Authorities, Long> {

    List<Authorities> findByAuthority(String authority);
}
