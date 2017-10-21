package io.cucumber.datatable;

import io.cucumber.cucumberexpressions.Function;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.cucumberexpressions.SingleTransformer;
import io.cucumber.cucumberexpressions.Transformer;
import io.cucumber.cucumberexpressions.TypeReference;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.cucumber.cucumberexpressions.TableType.tableOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;

public class ParameterTypeRegistryTableConverterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final ParameterTypeRegistry registry = new ParameterTypeRegistry(ENGLISH);
    private final TableConverter converter = new ParameterTypeRegistryTableConverter(registry);


    //    <T> T convert(DataTable dataTable, Type type, boolean transposed);
//
//    <T> List<T> asList(DataTable dataTable, Type itemType);

    @Test
    public void converts_empty_table_to_empty_list() {
        DataTable table = new DataTable(Collections.<List<String>>emptyList());
        assertEquals(emptyList(), converter.toList(table, Integer.class));
        assertEquals(emptyList(), converter.convert(table, new TypeReference<List<Integer>>() {
        }.getType(), false));

    }

    @Test
    public void converts_table_with_empty_row_to_empty_list() {
        DataTable table = new DataTable(singletonList(Collections.<String>emptyList()));
        assertEquals(emptyList(), converter.toList(table, Integer.class));
        assertEquals(emptyList(), converter.convert(table, new TypeReference<List<Integer>>() {
        }.getType(), false));
    }

    @Test
    public void to_list_cant_convert_to_list_of_unknown_type() {
        expectedException.expectMessage(String.format("Can't convert DataTable to List<%s>", Animal.class));
        DataTable table = new DataTable(
                singletonList(
                        singletonList("42")
                ));
        converter.toList(table, Animal.class);
    }

    @Test
    public void convert_cant_convert_to_list_of_unknown_type() {
        expectedException.expectMessage(String.format("Can't convert DataTable to List<%s>", Animal.class));
        DataTable table = new DataTable(
                singletonList(
                        singletonList("42")
                ));
        converter.convert(table, new TypeReference<List<Animal>>() {}.getType(), false);
    }


    @Test
    public void converts_table_of_single_column_to_list() {
        DataTable table = new DataTable(
                asList(
                        singletonList("3"),
                        singletonList("5"),
                        singletonList("6"),
                        singletonList("7")
                ));
        assertEquals(asList(3, 5, 6, 7), converter.toList(table, Integer.class));
        assertEquals(asList(3, 5, 6, 7), converter.convert(table, new TypeReference<List<Integer>>() {}.getType(), false));
    }


    @Test
    public void converts_table_of_several_columns_to_list() {
        DataTable table = new DataTable(
                asList(
                        asList("3", "5"),
                        asList("6", "7")
                ));
        assertEquals(asList(3, 5, 6, 7), converter.toList(table, Integer.class));
        assertEquals(asList(3, 5, 6, 7), converter.convert(table, new TypeReference<List<Integer>>() {}.getType(), false));

    }

    @Test
    public void when_converting_to_list_table_type_takes_precedence_over_item_type() {
        registry.defineParameterType(new ParameterType<>("animalId", "Animal[0-9]", Animal.class, new SingleTransformer<>(new Function<String, Animal>() {
            @Override
            public Animal apply(String animalId) {
                // Pretend this uses some lookup to find the animal by its animalId.
                return new Animal("Boomalope", 12);
            }
        })));

        registry.defineDataTableType(tableOf("animal", Animal.class, new Transformer<Map<String, String>, Animal>() {
            @Override
            public Animal transform(Map<String, String> values) {
                ParameterType<Integer> parameterType = registry.lookupByType(Integer.class);
                return new Animal(values.get("name"), parameterType.transform(singletonList(values.get("life expectancy"))));
            }
        }));

        DataTable table = new DataTable(asList(
                asList("name", "life expectancy"),
                asList("Muffalo", "15")
        ));

        assertEquals(singletonList(new Animal("Muffalo", 15)), converter.toList(table, Animal.class));
        assertEquals(singletonList(new Animal("Muffalo", 15)), converter.convert(table, new TypeReference<List<Animal>>() {}.getType(), false));
    }


    @Test
    public void converts_table_to_list_of_generic_item_type() {
        registry.defineDataTableType(tableOf("muffalo-barn", Barn.class, new Transformer<Map<String, String>, Barn>() {
            @Override
            public Barn transform(Map<String, String> values) {
                return new Barn<>(new Animal("Muffalo", 15));
            }
        }));

        DataTable table = new DataTable(asList(
                asList("name", "life expectancy"),
                asList("Muffalo", "15")
        ));

        assertEquals(singletonList(new Barn<>(new Animal("Muffalo", 15))), converter.toList(table, Barn.class));
        assertEquals(singletonList(new Barn<>(new Animal("Muffalo", 15))), converter.convert(table, new TypeReference<List<Barn<Animal>>>() {}.getType(), false));
    }


//    <T> List<List<T>> asLists(DataTable dataTable, Type itemType);

    @Test
    public void converts_empty_table_to_empty_lists() {
        DataTable table = new DataTable(Collections.<List<String>>emptyList());
        assertEquals(emptyList(), converter.toLists(table, Integer.class));
        assertEquals(emptyList(), converter.convert(table, new TypeReference<List<List<Integer>>>() {}.getType(), false));
    }

    @Test
    public void converts_table_with_empty_row_to_list_of_empty_lists() {
        DataTable table = new DataTable(singletonList(Collections.<String>emptyList()));
        assertEquals(singletonList(emptyList()), converter.toLists(table, Integer.class));
        assertEquals(singletonList(emptyList()), converter.convert(table, new TypeReference<List<List<Integer>>>() {}.getType(), false));
    }


    @Test
    public void converts_table_of_single_column_to_lists() {
        DataTable table = new DataTable(
                asList(
                        singletonList("3"),
                        singletonList("5"),
                        singletonList("6"),
                        singletonList("7")
                ));

        List<List<Integer>> expected = asList(
                singletonList(3),
                singletonList(5),
                singletonList(6),
                singletonList(7));

        assertEquals(expected, converter.toLists(table, Integer.class));
        assertEquals(expected, converter.convert(table, new TypeReference<List<List<Integer>>>() {}.getType(), false));
    }

    @Test
    public void converts_table_of_several_columns_to_lists() {
        DataTable table = new DataTable(
                asList(
                        asList("3", "5"),
                        asList("6", "7")
                ));

        List<List<Integer>> expected = asList(
                asList(3, 5),
                asList(6, 7));

        assertEquals(expected, converter.toLists(table, Integer.class));
        assertEquals(expected, converter.convert(table, new TypeReference<List<List<Integer>>>() {}.getType(), false));
    }

    @Test
    public void to_lists_cant_convert_to_lists_of_unknown_type() {
        DataTable table = new DataTable(
                singletonList(
                        singletonList("42")
                ));


        expectedException.expectMessage(String.format("Can't convert DataTable to List<List<%s>>", Animal.class));
        converter.toLists(table, Animal.class);
    }

    @Test
    public void convert_cant_convert_to_lists_of_unknown_type() {
        DataTable table = new DataTable(
                singletonList(
                        singletonList("42")
                ));


        expectedException.expectMessage(String.format("Can't convert DataTable to List<List<%s>>", Animal.class));
        converter.convert(table, new TypeReference<List<List<Animal>>>() {}.getType(), false);
    }


//      //TODO: Add convert cases below here.
//    <K, V> Map<K, V> asMap(DataTable dataTable, Type keyType, Type valueType);

    @Test
    public void converts_empty_table_to_empty_map() {
        DataTable table = new DataTable(Collections.<List<String>>emptyList());
        assertEquals(emptyMap(), converter.toMap(table, Integer.class, Integer.class));
    }

    @Test
    public void cant_convert_table_with_empty_row_to_map() {
        expectedException.expectMessage("A DataTable can only be converted to a Map when there are 2 columns");

        DataTable table = new DataTable(singletonList(Collections.<String>emptyList()));
        converter.toMap(table, Integer.class, Integer.class);
    }

    @Test
    public void converts_table_of_two_columns_to_map() {
        DataTable table = new DataTable(
                asList(
                        asList("3", "c"),
                        asList("5", "e"),
                        asList("6", "f")));


        Map<Integer, String> expected = new HashMap<Integer, String>() {{
            put(3, "c");
            put(5, "e");
            put(6, "f");
        }};

        assertEquals(expected, converter.toMap(table, Integer.class, String.class));
    }


    @Test
    public void cant_convert_to_map_of_unknown_key_type() {
        expectedException.expectMessage(String.format("Can't convert DataTable to Map<%s,%s>", Animal.class, String.class));

        DataTable table = new DataTable(
                asList(
                        asList("Alphabeaver", "Hare"),
                        asList("Cassowary", "Husky"),
                        asList("Megasloth", "Spelopede")));

        converter.toMap(table, Animal.class, String.class);
    }


    @Test
    public void cant_convert_to_map_of_unknown_value_type() {
        expectedException.expectMessage(String.format("Can't convert DataTable to Map<%s,%s>", String.class, Animal.class));

        DataTable table = new DataTable(
                asList(
                        asList("Alphabeaver", "Hare"),
                        asList("Cassowary", "Husky"),
                        asList("Megasloth", "Spelopede")));

        converter.toMap(table, String.class, Animal.class);
    }

    //
//    <K, V> List<Map<K, V>> asMaps(DataTable dataTable, Type keyType, Type valueType);

    @Test
    public void converts_table_to_maps() {
        DataTable table = new DataTable(
                asList(
                        asList("1", "2", "3"),
                        asList("4", "5", "6"),
                        asList("7", "8", "9")));

        List<HashMap<Integer, Integer>> expected =
                asList(
                        new HashMap<Integer, Integer>() {{
                            put(1, 4);
                            put(2, 5);
                            put(3, 6);
                        }},
                        new HashMap<Integer, Integer>() {{
                            put(1, 7);
                            put(2, 8);
                            put(3, 9);
                        }}
                );

        assertEquals(expected, converter.toMaps(table, Integer.class, Integer.class));
    }

    @Test
    public void converts_empty_table_to_empty_list_of_maps() {
        DataTable table = new DataTable(Collections.<List<String>>emptyList());
        assertEquals(emptyList(), converter.toMaps(table, Integer.class, Integer.class));
    }

    @Test
    public void converts_table_with_single_row_to_empty_list_of_maps() {
        DataTable table = new DataTable(singletonList(asList("1", "2", "3")));
        assertEquals(emptyList(), converter.toMaps(table, Integer.class, Integer.class));
    }


    @Test
    public void cant_convert_to_maps_of_unknown_key_type() {
        expectedException.expectMessage(String.format("Can't convert DataTable to List<Map<%s,%s>>", Animal.class, String.class));

        DataTable table = new DataTable(
                asList(
                        asList("Alphabeaver", "Hare"),
                        asList("Cassowary", "Husky"),
                        asList("Megasloth", "Spelopede")));

        converter.toMaps(table, Animal.class, String.class);
    }


    @Test
    public void cant_convert_to_maps_of_unknown_value_type() {
        expectedException.expectMessage(String.format("Can't convert DataTable to List<Map<%s,%s>>", String.class, Animal.class));

        DataTable table = new DataTable(
                asList(
                        asList("Alphabeaver", "Hare"),
                        asList("Cassowary", "Husky"),
                        asList("Megasloth", "Spelopede")));

        converter.toMaps(table, String.class, Animal.class);
    }


    private final class Animal {
        private final String name;
        private final int lifeExpectancy;

        private Animal(String name, int lifeExpectancy) {
            this.name = name;
            this.lifeExpectancy = lifeExpectancy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Animal animal = (Animal) o;
            return lifeExpectancy == animal.lifeExpectancy &&
                    Objects.equals(name, animal.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, lifeExpectancy);
        }
    }

    private final class Barn<A extends Animal> {

        final A animal;

        private Barn(A animal) {
            this.animal = animal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Barn<?> barn = (Barn<?>) o;
            return Objects.equals(animal, barn.animal);
        }

        @Override
        public int hashCode() {
            return Objects.hash(animal);
        }
    }
}
