package diff;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.reflect.Modifier.isStatic;
import static org.springframework.util.ReflectionUtils.getField;

@Slf4j
public final class ObjectDiffer {

    private ObjectDiffer() {
    }

    public static DiffMap diff(Object modified, Object actual, Class<?> tClass) {
        return recursiveDiff(modified, actual, tClass);
    }
    private static DiffMap recursiveDiff(Object modified, Object actual, Class<?> tClass) {
        DiffMap diffObject = new DiffMap();
        getAllFields(tClass).stream()
            .filter(field -> !isStatic(field.getModifiers()) &&
                field.getAnnotation(IncludeInDiff.class) != null)
            .forEach(field -> {
                field.setAccessible(true);
                Object actualValue = getVal(actual, field);
                Object modifiedValue = getVal(modified, field);
                processDiffField(diffObject, field, actualValue, modifiedValue);
            });
        return MapUtils.isNotEmpty(diffObject) ? diffObject : null;
    }

    private static void processDiffFieldWithChild(DiffMap diffObject, Field field,
                                                  Object actualValue, Object modifiedValue) {
        DiffMap childDiff = recursiveDiff(modifiedValue, actualValue, field.getType());
        if (MapUtils.isNotEmpty(childDiff)) {
            diffObject.put(field.getName(), childDiff);
        }
    }

    private static void processDiffField(DiffMap diffObject, Field field,
                                         Object actualValue, Object modifiedValue) {
        if (field.getAnnotation(IncludeInDiff.class).hasChild() && modifiedValue != null) {
            processDiffFieldWithChild(diffObject, field, actualValue, modifiedValue);
        } else if (!Objects.equals(actualValue, modifiedValue)) {
            diffObject.put(field.getName(), modifiedValue);
        }
    }

    private static List<Field> getAllFields(Class<?> type) {
        return getAllFields(new ArrayList<>(), type);
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }
        return fields;
    }

    private static Object getVal(Object object, Field field) {
        return object == null ? null : getField(field, object);
    }
}
