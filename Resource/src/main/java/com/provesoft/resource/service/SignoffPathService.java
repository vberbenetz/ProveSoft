package com.provesoft.resource.service;

import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathId;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSeq;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.repository.SignoffPathIdRepository;
import com.provesoft.resource.repository.SignoffPathRepository;
import com.provesoft.resource.repository.SignoffPathSeqRepository;
import com.provesoft.resource.repository.SignoffPathStepsRepository;
import com.provesoft.resource.utils.SignoffPathHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.transaction.TransactionRolledbackException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SignoffPathService {

    @Autowired
    SignoffPathRepository signoffPathRepository;

    @Autowired
    SignoffPathIdRepository signoffPathIdRepository;

    @Autowired
    SignoffPathSeqRepository signoffPathSeqRepository;

    @Autowired
    SignoffPathStepsRepository signoffPathStepsRepository;


    /* ------------------------ SignoffPath -------------------------- */

    public SignoffPath findByCompanyNameAndPathId(String companyName, Long pathId) {
        return signoffPathRepository.findByKeyCompanyNameAndKeyPathId(companyName, pathId);
    }

    public List<SignoffPath> findFirst10ByCompanyNameAndPathId(String companyName, Long pathId) {
        return signoffPathRepository.findFirst10ByKeyCompanyNameAndKeyPathIdLikeOrderByKeyPathIdAsc(companyName, pathId);
    }

    public List<SignoffPath> findFirst10ByCompanyNameAndPathName(String companyName, String pathName) {
        return signoffPathRepository.findFirst10ByKeyCompanyNameAndNameLikeOrderByNameAsc(companyName, pathName);
    }

    public List<SignoffPath> findFirst10ByCompanyName(String companyName) {
        return signoffPathRepository.findFirst10ByKeyCompanyNameOrderByKeyPathIdAsc(companyName);
    }

    public List<SignoffPath> getPathsByOrganizationId(String companyName, Long organizationId) {
        return signoffPathRepository.getPathsByCompanyNameAndOrganizationId(companyName, organizationId);
    }

    // Create signoff path and create initial sequence object
    public SignoffPath createNewPath(SignoffPath signoffPath) {
        SignoffPath newSignoffPath = signoffPathRepository.saveAndFlush(signoffPath);
        SignoffPathSeq newSeq = new SignoffPathSeq(newSignoffPath.getKey().getCompanyName(), newSignoffPath.getKey().getPathId(), "");
        signoffPathSeqRepository.saveAndFlush(newSeq);
        return newSignoffPath;
    }


    /* ------------------------ SignoffPathId -------------------------- */

    /*
        Get and increment SignoffPath Id
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SignoffPathId getAndIncrementSignoffPathId (String companyName) throws TransactionRolledbackException {
        SignoffPathId newPathId = signoffPathIdRepository.findByKeyCompanyName(companyName);
        signoffPathIdRepository.incrementPathId(companyName);
        signoffPathIdRepository.flush();
        return newPathId;
    }


    /* ------------------------ SignoffPathSteps -------------------------- */

    /*
        Retrieve all steps for SignoffPath
     */
    public List<SignoffPathSteps> getStepsForPath(String companyName, Long pathId) {
        return signoffPathStepsRepository.findByCompanyNameAndPathIdOrderByIdAsc(companyName, pathId);
    }

    /*
        Retrieve step
     */
    public SignoffPathSteps getStep(String companyName, Long pathId, Long stepId) {
        return signoffPathStepsRepository.findByCompanyNameAndPathIdAndId(companyName, pathId, stepId);
    }

    /*
        Create new path step (single)
     */
    public SignoffPathSteps createNewStep(SignoffPathSteps signoffPathStep) {
        return signoffPathStepsRepository.saveAndFlush(signoffPathStep);
    }

    /*
        Create new path steps (multiple)
     */
    public List<SignoffPathSteps> createNewSteps(List<SignoffPathSteps> signoffPathSteps) {
        List<SignoffPathSteps> retList = signoffPathStepsRepository.save(signoffPathSteps);
        signoffPathStepsRepository.flush();
        return retList;
    }

    /*
        Delete a signoff step
     */
    public void deleteSignoffSteps(List<SignoffPathSteps> stepsToDelete) {
        signoffPathStepsRepository.deleteInBatch(stepsToDelete);
        signoffPathStepsRepository.flush();
    }


    /* ------------------------ SignoffPathSeq -------------------------- */

    /*
        Retrieve sign off path sequence
     */
    public SignoffPathSeq getPathSeq(String companyName, Long pathId) {
        return signoffPathSeqRepository.findByKeyCompanyNameAndKeyPathId(companyName, pathId);
    }

    /*
        Update path steps sequence string
     */
    public void appendToPathSeq(String companyName, List<SignoffPathSteps> newSignoffPathSteps) {

        Long pathId = newSignoffPathSteps.get(0).getPathId();
        SignoffPathSeq signoffPathSeq = signoffPathSeqRepository.findByKeyCompanyNameAndKeyPathId(companyName, pathId);

        String pathSequence = signoffPathSeq.getPathSequence();

        for (SignoffPathSteps s : newSignoffPathSteps) {
            pathSequence = pathSequence + s.getId() + "|";
        }

        signoffPathSeq.setPathSequence(pathSequence);
        signoffPathSeqRepository.saveAndFlush(signoffPathSeq);
    }

    /*
        Remove path steps from sequence
     */
    public void removeFromPathSeq(String companyName, Long pathId, List<SignoffPathSteps> pathStepsToRemove) {
        SignoffPathSeq signoffPathSeq = signoffPathSeqRepository.findByKeyCompanyNameAndKeyPathId(companyName, pathId);

        List<String> pathSeq = Arrays.asList( signoffPathSeq.getPathSequence().split("\\|") );
        String newPathSeq = "";

        for (String stepId: pathSeq) {

            boolean removed = false;

            for (SignoffPathSteps s: pathStepsToRemove) {
                if ( s.getId().toString().equals(stepId) ) {
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                newPathSeq = newPathSeq + stepId + "|";
            }
        }

        signoffPathSeq.setPathSequence(newPathSeq);
        signoffPathSeqRepository.saveAndFlush(signoffPathSeq);
    }

}
