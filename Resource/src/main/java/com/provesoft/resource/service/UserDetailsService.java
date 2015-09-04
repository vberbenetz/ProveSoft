package com.provesoft.resource.service;

import com.provesoft.resource.entity.ProfilePicture;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.entity.UserPermissions;
import com.provesoft.resource.repository.ProfilePictureRepository;
import com.provesoft.resource.repository.UserDetailsRepository;
import com.provesoft.resource.repository.UserPermissionsRepository;
import com.provesoft.resource.utils.ProfilePicturePkg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.tomcat.util.codec.binary.StringUtils;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.List;

/**
 * Service encompasses all routes and methods regarding User properties and details. This excludes all sensitive
 * data such as authentication and user authorities.
 */
@Service
public class UserDetailsService {

    @Autowired
    UserDetailsRepository userDetailsRepository;

    @Autowired
    UserPermissionsRepository userPermissionsRepository;

    @Autowired
    ProfilePictureRepository profilePictureRepository;


    public List<UserDetails> findAllByCompanyName(String companyName) {
        return userDetailsRepository.findAllByCompanyNameOrderByLastNameAsc(companyName);
    }

    public List<UserDetails> findFirst10ByCompanyName(String companyName) {
        return userDetailsRepository.findFirst10ByCompanyNameOrderByLastNameAsc(companyName);
    }

    public UserDetails findByCompanyNameAndUserId(String companyName, Long userId) {
        return userDetailsRepository.findByCompanyNameAndUserId(companyName, userId);
    }

    public UserDetails findByCompanyNameAndEmail(String companyName, String email) {
        return userDetailsRepository.findByCompanyNameAndEmail(companyName, email);
    }

    public List<UserDetails> findByCompanyNameAndUserIdList(String companyName, List<Long> userIds) {
        return userDetailsRepository.findByCompanyNameAndUserIdIn(companyName, userIds);
    }

    /**
     * Method is used to perform a wildcard lookup for a user based on a partial first or last name.
     * @param companyName Company query parameter
     * @param name Partial first or last name string
     * @return List of UserDetails
     */
    public List<UserDetails> findByCompanyAndPartialName(String companyName, String name) {
        List<UserDetails> results = userDetailsRepository.findByCompanyAndPartialName(companyName, name);
        int upToIndex = 0;

        if (results.size() < 20) {
            upToIndex = results.size();
        }
        else {
            upToIndex = 20;
        }

        return results.subList(0, upToIndex);
    }

    public Long findUserIdByCompanyNameAndEmail(String companyName, String email) {
        return userDetailsRepository.findUserIdByCompanyNameAndEmail(companyName, email);
    }

    public UserDetails addUser(UserDetails newUser) {
        return userDetailsRepository.saveAndFlush(newUser);
    }

    public void deleteByUserId(String companyName, Long userId) {
        userDetailsRepository.deleteByUserId(companyName, userId);
    }


    /* ---------------------- UserPermissions -------------------- */

    public List<UserPermissions> findUserPermissionsByUserId(Long userId) {
        return userPermissionsRepository.findByKeyUserId(userId);
    }

    public List<UserPermissions> addUserPermissions(List<UserPermissions> userPermissions) {
        List<UserPermissions> savedUserPermissions = userPermissionsRepository.save(userPermissions);
        userPermissionsRepository.flush();
        return savedUserPermissions;
    }

    public void deleteList(List<UserPermissions> userPermissions) {
        userPermissionsRepository.deleteInBatch(userPermissions);
        userPermissionsRepository.flush();
    }

    public void deleteAllPermissions(Long userId) {
        userPermissionsRepository.deleteByUserId(userId);
    }


    /* ---------------------- Profile Picture -------------------- */

    /**
     * Method retrieves the profile picture for the user and convert the picture data to a Base64 string.
     * It is preceeded with the necessary metadata in order for browsers to render the image based on the string.
     * @param companyName Company query parameter
     * @param userId User id of user whose profile picture is being retrieved
     * @return ProfilePicturePkg
     */
    public ProfilePicturePkg findProfilePictureForUser (String companyName, Long userId) {

        ProfilePicture pic = profilePictureRepository.findByKeyCompanyNameAndKeyUserId(companyName, userId);

        // No profile picture found
        if (pic == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("data:image/png;base64,");
        sb.append( StringUtils.newStringUtf8( Base64.encodeBase64(pic.getImage(), false) ) );

        return new ProfilePicturePkg(userId, sb.toString());
    }

    /**
     * Method retrieves profile picture for multiple users and converts them to Base64 strings.
     * These strings are preceeded with metadata for the browser to render them correctly.
     * @param companyName Company query parameter
     * @param userIds List of userIds for which the profile pictures are being retrieved for
     * @return List of ProfilePicturePkg
     */
    public List<ProfilePicturePkg> findProfilePicturesByIds (String companyName, Long[] userIds) {

        List<ProfilePicture> pics = profilePictureRepository.findByKeyCompanyNameAndKeyUserIdIn(companyName, userIds);

        // No profile picture found
        if (pics.size() == 0) {
            return null;
        }

        List<ProfilePicturePkg> retList = new ArrayList<>();
        for (ProfilePicture pic : pics) {
            StringBuilder sb = new StringBuilder();
            sb.append("data:image/png;base64,");
            sb.append( StringUtils.newStringUtf8( Base64.encodeBase64(pic.getImage(), false) ) );
            retList.add( new ProfilePicturePkg(pic.getKey().getUserId(), sb.toString()) );
        }

        return retList;
    }

    public void uploadProfilePicture (ProfilePicture newPic) {
        profilePictureRepository.saveAndFlush(newPic);
    }

}
