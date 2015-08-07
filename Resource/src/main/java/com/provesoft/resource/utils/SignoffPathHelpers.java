package com.provesoft.resource.utils;

import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.exceptions.InternalServerErrorException;

import java.util.List;

public final class SignoffPathHelpers {

    private SignoffPathHelpers() {}

    public static String removeFromStepSequence(String stepSequence, Long pathStepIdToRemove) {
        try {
            String retSeq = "";
            String[] sequences = stepSequence.split("\\|");

            for (String seq : sequences) {
                if ( seq.equals(pathStepIdToRemove.toString()) ) {
                    continue;
                }
                retSeq = retSeq + seq + "|";
            }

            return retSeq;
        }
        catch (Exception ex) {
            throw new InternalServerErrorException();
        }
    }

    public static String generateSeqWithActions(String seq, List<SignoffPathSteps> steps) {
        try {
            String retSeq = "";
            String[] sequences = seq.split("\\|");

            for (String s : sequences) {
                for (SignoffPathSteps step : steps) {
                    if ( step.getId().toString().equals(s) ) {
                        switch(step.getAction()) {
                            case "THEN":
                                retSeq += "&" + s;
                                break;
                            case "OR":
                                retSeq += "|" + s;
                                break;
                            default:
                                retSeq += s;
                                break;
                        }
                    }
                }
            }

            return retSeq;
        }
        catch (Exception ex) {
            throw new InternalServerErrorException();
        }
    }
}
