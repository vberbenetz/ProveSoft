package com.provesoft.gateway.repository;

import com.provesoft.gateway.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, String> {
}
