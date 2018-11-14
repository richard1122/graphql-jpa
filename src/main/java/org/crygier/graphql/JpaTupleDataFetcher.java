package org.crygier.graphql;

import graphql.execution.ExecutionTypeInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.PropertyDataFetcher;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

public class JpaTupleDataFetcher implements DataFetcher {

    private final String name;
    private final DataFetcher innerDataFetcher;

    public JpaTupleDataFetcher(String name) {
        this.name = name;
        innerDataFetcher = new PropertyDataFetcher(name);
    }

    private String getAlias(ExecutionTypeInfo typeInfo) {
        if (typeInfo == null || typeInfo.getFieldDefinition() == null) return null;
        String name = typeInfo.getFieldDefinition().getName();
        String parent = getAlias(typeInfo.getParentTypeInfo());
        return (parent == null ? "" : parent + ".") + name;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Object source = environment.getSource();
        String alias = getAlias(environment.getFieldTypeInfo());

        if (source instanceof Tuple) {
            Tuple tuple = (Tuple) source;
            if (environment.getFieldDefinition().getType() instanceof GraphQLObjectType) {
                int index = 0;
                for (TupleElement<?> element : tuple.getElements()) {
                    if (element.getAlias().startsWith(alias + ".") && tuple.get(index) != null) return tuple;
                    ++index;
                }
                return null;
            }
            try {
                return tuple.get(alias);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            return innerDataFetcher.get(environment);
        }
    }

}
