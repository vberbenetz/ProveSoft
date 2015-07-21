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
        Create new path step
     */
    public SignoffPathSteps createNewStep(SignoffPathSteps signoffPathStep) {
        return signoffPathStepsRepository.saveAndFlush(signoffPathStep);
    }


    /* ------------------------ SignoffPathSeq -------------------------- */

    /*
        Update path steps sequence string
     */
    public void appendToPathSeq(String companyName, Long pathId, Long pathStepIdToAppend) {
        SignoffPathSteps signoffPathStep = signoffPathStepsRepository.findByCompanyNameAndPathId(companyName, pathId);
        signoffPathStep.setStepSequence( signoffPathStep.getStepSequence() + pathStepIdToAppend.toString() + "|" );
        signoffPathStepsRepository.saveAndFlush(signoffPathStep);
    }

    /*
        Remove path step from sequence
     */
    public void removeFromPathSeq(String companyName, Long pathId, Long pathStepIdToRemove) {
        SignoffPathSteps signoffPathStep = signoffPathStepsRepository.findByCompanyNameAndPathId(companyName, pathId);

        String stepSequence = signoffPathStep.getStepSequence();
        stepSequence = SignoffPathHelpers.removeFromStepSequence( stepSequence , pathStepIdToRemove );
        signoffPathStep.setStepSequence(stepSequence);
    }

}
