package com.kheops.csv.reader.reflect;

import com.kheops.csv.reader.reflect.converters.ConversionSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class InstanceCreatorJavaTest {
    private final InstanceCreator underTest = new InstanceCreator();

    @Nested
    class WhenCreatingAJavaClass {
        @Nested
        class GivenProvidedArgumentsAreValid {
            private final List<InstantiationArgument> arguments = Arrays.asList(
                    new InstantiationArgument(
                            new InstantiationField(
                                    JavaTestClass.class.getField("value"),
                                    null
                            ),
                            "value",
                            "original_name"
                    ),
                    new InstantiationArgument(
                            new InstantiationField(
                                    JavaTestClass.class.getField("intValue"),
                                    null
                            ),
                            "12",
                            "original_name_int"
                    )
            );

            private final JavaTestClass expected = new JavaTestClass(
                    "value",
                    12
            );

            GivenProvidedArgumentsAreValid() throws NoSuchFieldException {
            }

            @Test
            void thenConstructionSucceeds() {
                final var result = underTest.createInstance(JavaTestClass.class, arguments, new ConversionSettings());
                Assertions.assertEquals(expected, result.getResult());
                Assertions.assertTrue(result.getErrors().isEmpty());
            }
        }
    }
}

