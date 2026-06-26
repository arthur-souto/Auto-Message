package com.arthursouto.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

public class UnaccentFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributor) {
        functionContributor.getFunctionRegistry()
                .registerPattern("unaccent", "unaccent(?1)",
                        functionContributor.getTypeConfiguration()
                                .getBasicTypeRegistry()
                                .resolve(StandardBasicTypes.STRING)
                );
    }
}
