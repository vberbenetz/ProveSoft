package com.provesoft.resource.utils;

import com.provesoft.resource.exceptions.InternalServerErrorException;

public final class SignoffPathHelpers {

    private SignoffPathHelpers() {}

    public static String removeFromStepSequence(String stepSequence, Long pathStepIdToRemove) {
        try {
            String retSeq = "";
            String[] sequences = stepSequence.split("|");

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
}
