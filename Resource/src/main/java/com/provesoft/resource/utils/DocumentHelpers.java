package com.provesoft.resource.utils;

/**
 * Class contains helper methods revolving Documents
 */
public final class DocumentHelpers {

    private DocumentHelpers() {}

    /**
     * Method determines the next revision Id, and handles digit rollover (ex: Z -&amp; AA)
     * @param currentRevId Current revision Id
     * @return Next revision Id String
     */
    public static String genNextRevId(String currentRevId) {
        String nextRevId = "";
        char lastIdChar = currentRevId.charAt(currentRevId.length() - 1);
        currentRevId = currentRevId.substring(0, currentRevId.length() - 1);

        // Increment all letter digits of id
        while (currentRevId.length() > 0) {
            if (lastIdChar == 'Z') {
                nextRevId = "A" + nextRevId;
            }
            else {
                return currentRevId + (char)(lastIdChar + 1) + nextRevId;
            }

            lastIdChar = currentRevId.charAt(currentRevId.length() - 1);
            currentRevId = currentRevId.substring(0, currentRevId.length() - 1);
        }

        // Case where all Z's (Ex: ZZZ). Needs to role over to AAAA
        if(lastIdChar == 'Z') {
            return "AA" + nextRevId;
        }
        else {
            return (char)(lastIdChar + 1) + nextRevId;
        }
    }
}
