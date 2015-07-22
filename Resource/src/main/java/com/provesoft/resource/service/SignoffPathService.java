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
        Retrieve all steps for SignoffPath
     */
    public List<SignoffPathSteps> getStepsForPath(String companyName, Long pathId) {
        return signoffPathStepsRepository.findByCompanyNameAndPathIdOrderByIdAsc(companyName, pathId);
    }

    /*
        Create new path step
     */
    public List<SignoffPathSteps> createNewSteps(List<SignoffPathSteps> signoffPathSteps) {
        List<SignoffPathSteps> retList = signoffPathStepsRepository.save(signoffPathSteps);
        signoffPathStepsRepository.flush();
        return retList;
    }


    /* ------------------------ SignoffPathSeq -------------------------- */

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
        Remove path step from sequence
     */
    public void removeFromPathSeq(String companyName, Long pathId, Long pathStepIdToRemove) {
        SignoffPathSeq signoffPathSeq = signoffPathSeqRepository.findByKeyCompanyNameAndKeyPathId(companyName, pathId);

        String pathSequence = signoffPathSeq.getPathSequence();
        pathSequence = SignoffPathHelpers.removeFromStepSequence( pathSequence , pathStepIdToRemove );
        signoffPathSeq.setPathSequence(pathSequence);
    }

}
