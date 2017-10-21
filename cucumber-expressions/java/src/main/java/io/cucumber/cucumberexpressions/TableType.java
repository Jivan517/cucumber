package io.cucumber.cucumberexpressions;

import io.cucumber.datatable.DataTable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableType<T>  implements ParameterTransform<DataTable, T>, Comparable<TableType<?>> {
    private final String name;
    private final Type type;
    private final Transformer<DataTable, T> transformer;

    TableType(String name, Type type, Transformer<DataTable, T> transformer) {
        if (name == null) throw new CucumberExpressionException("name cannot be null");
        if (type == null) throw new CucumberExpressionException("type cannot be null");
        if (transformer == null) throw new CucumberExpressionException("transformer cannot be null");
        this.name = name;
        this.type = type;
        this.transformer = transformer;
    }

    /**
     * This is used in the type name in typed expressions
     *
     * @return human readable type name
     */
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }


    public int compareTo(TableType<?> o) {
        return getName().compareTo(o.getName());
    }

    public T transform(DataTable tableRow) {
        return transformer.transform(tableRow);
    }


    public static <T> TableType<T> tableAs(String name, Class<T> type, final Transformer<Map<String, String>, T> transformer) {
        return new TableType<>(name, type, new Transformer<DataTable, T>() {
            @Override
            public T transform(DataTable values) {
                return transformer.transform(values.transpose().asMaps().get(0));
            }
        });
    }

    public static <T> TableType<List<T>> tableOf(String name, Class<T> type, final Transformer<Map<String, String>, T> transformer) {
        return new TableType<>(name, aListOf(type), new Transformer<DataTable, List<T>>() {
            @Override
            public List<T> transform(DataTable table) {
                List<Map<String, String>> rows = table.asMaps();
                List<T> list = new ArrayList<>(rows.size());
                for (Map<String, String> row : rows) {
                    list.add(transformer.transform(row));
                }

                return list;
            }
        });
    }

    public static Type aListOf(final Type type) {
        //TODO: Quick fake out. This works because we the parameter registry uses toString.
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{type};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }

            @Override
            public String toString() {
                if(type instanceof  Class){
                    return List.class.getName() + "<" + ((Class) type).getName() + ">";
                }

                return List.class.getName() + "<" + type.toString() + ">";
            }
        };
    }

}