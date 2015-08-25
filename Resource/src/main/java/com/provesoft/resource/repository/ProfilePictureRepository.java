package com.provesoft.resource.repository;

import com.provesoft.resource.entity.ProfilePicture;
import com.provesoft.resource.entity.ProfilePictureKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, ProfilePictureKey> {

    ProfilePicture findByKeyCompanyNameAndKeyUserId(String companyName, Long userId);

    List<ProfilePicture> findByKeyCompanyNameAndKeyUserIdIn(String companyName, Long[] userIds);
}
