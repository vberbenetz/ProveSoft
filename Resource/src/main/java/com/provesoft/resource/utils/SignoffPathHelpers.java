package com.provesoft.resource.utils;

import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;

import java.util.ArrayList;
import java.util.List;

/**
 * Class contains methods to help with filtering and grouping SignoffPathSteps
 */
public final class SignoffPathHelpers {

    private SignoffPathHelpers() {}

    /**
     * Method retrieves next set of steps by looking for subsequent "THEN" action
     * @param steps Current and future SignoffPathSteps (no previous ones)
     * @return List of SignoffPathSteps
     */
    public static List<SignoffPathSteps> extractNextSetOfSteps(List<SignoffPathSteps> steps) {
        List<SignoffPathSteps> setOfSteps = new ArrayList<>();
        for (SignoffPathSteps s : steps) {
            if (s.getAction().equals("THEN")) {
                break;
            }
            else {
                setOfSteps.add(s);
            }
        }

        return setOfSteps;
    }

    /**
     * Method retrieves set of steps which contains the passed in stepId parameter
     * @param steps Complete list of SignoffPathSteps
     * @param stepId Step id used to find its group of steps
     * @return List of SignoffPathSteps
     */
    public static List<SignoffPathSteps> getStepsInGroup(List<SignoffPathSteps> steps, Long stepId) {

        List<SignoffPathSteps> stepsInGroup = new ArrayList<>();

        for (int i = 0; i < steps.size(); i++) {

            // Found matching step.
            // Traverse in reverse to get to starting step, then add all steps until end of group to returning list.
            if (steps.get(i).getId().equals(stepId)) {

                // Traverse in revers to find start of group
                while ( (i > 0) && (steps.get(i).getAction().equals("OR")) ) {
                    i--;
                }

                // Add initial THEN or START step
                stepsInGroup.add(steps.get(i));
                i++;

                // Fetch all steps in group
                for (int j = i; j < steps.size(); j++) {
                    if (!steps.get(j).getAction().equals("OR")) {
                        break;
                    }

                    stepsInGroup.add(steps.get(j));
                }
            }
        }

        return stepsInGroup;
    }

    /**
     * Method is only called on a new set of SignoffPathSteps. It extracts the first set of steps from the group.
     * @param nonApprovedSteps Group of SignoffPathSteps
     * @return List of SignoffPathSteps
     */
    public static List<SignoffPathSteps> getNextGroupOfSteps(List<SignoffPathSteps> nonApprovedSteps) {

        List<SignoffPathSteps> nextGroupOfSteps = new ArrayList<>();

        // Add first Step because it is "START" or "THEN" step
        nextGroupOfSteps.add(nonApprovedSteps.get(0));

        // Traverse and add remaining steps in group to list
        for (int i = 1; i < nonApprovedSteps.size(); i++) {
            if (!nonApprovedSteps.get(i).getAction().equals("OR")) {
                break;
            }
            nextGroupOfSteps.add(nonApprovedSteps.get(i));
        }

        return nextGroupOfSteps;
    }

}
