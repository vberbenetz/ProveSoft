package com.provesoft.resource.repository;


import com.provesoft.resource.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

    List<UserDetails> findAllByCompanyName(String companyName);

    List<UserDetails> findFirst10ByCompanyName(String companyName);

    String findCompanyNameByUserId(Long userId);
}
