package com.springboot.demo.util;

import java.util.Arrays;
import java.util.function.Predicate;

import com.adp.core.domain.EnvironmentType;

public class MetadataUtil {

    public static final String ENTITY_ID = "adp";
    public static final String HYPHEN = "-";

    public static String getMetaDataEntityID(String[] environments) {
        Predicate<String[]> predicateProd = (String[] envs) -> Arrays.asList(envs)
                .contains(EnvironmentType.PROD.getCode());
        Predicate<String[]> predicateDev = (String[] envs) -> Arrays.asList(envs)
                .contains(EnvironmentType.DEVL.getCode());
        Predicate<String[]> predicateQa = (String[] envs) -> Arrays.asList(envs).contains(EnvironmentType.QA.getCode());
        if (predicateProd.test(environments)) {
            return ENTITY_ID;
        } else if (predicateDev.test(environments)) {
            return ENTITY_ID + HYPHEN + EnvironmentType.DEVL.getCode();
        } else if (predicateQa.test(environments)) {
            return ENTITY_ID + HYPHEN + EnvironmentType.QA.getCode();
        }
        return ENTITY_ID + HYPHEN + EnvironmentType.LOCAL.getCode();

    }

}
