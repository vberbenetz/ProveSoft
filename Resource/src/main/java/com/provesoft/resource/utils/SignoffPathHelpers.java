package com.provesoft.resource.utils;

import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import com.provesoft.resource.exceptions.InternalServerErrorException;

import java.util.ArrayList;
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

    // Extract first set of sequence step Id's from action string
    public static List<Long> extractInitialSetOfIdsFromActionString(String seq) {
        try {
            List<Long> retList = new ArrayList<>();

            String[] firstGroup = seq.split("&")[0].split("\\|");
            for (String id : firstGroup) {
                retList.add(Long.parseLong(id));
            }

            return retList;
        }
        catch (Exception ex) {
            throw new InternalServerErrorException();
        }
    }
}
