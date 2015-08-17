package com.provesoft.resource.utils;

import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;

import java.util.ArrayList;
import java.util.List;

public final class SignoffPathHelpers {

    private SignoffPathHelpers() {}

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
