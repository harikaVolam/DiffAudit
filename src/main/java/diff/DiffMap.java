package diff;

import lombok.NoArgsConstructor;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DiffMap extends LinkedHashMap<String, Object> {

    private static Object getLastPathElement(Object[] pathElements) {
        return pathElements[pathElements.length - 1];
    }

    private static Object[] getPathElements(Object key) {
        if (key instanceof String) {
            return ((String) key).split("\\.");
        }
        return new Object[0];
    }

    private static Map<?, ?> getIfMap(Object key, Map<?, ?> map) {
        Map<?, ?> returnValue = null;
        if (map != null && map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof Map) {
                returnValue = (Map) value;
            }
        }
        return returnValue;
    }

    @Override
    public boolean containsKey(Object key) {
        Object[] pathElements = getPathElements(key);
        if (pathElements != null && pathElements.length > 1) {
            return this.containsPath(pathElements);
        }
        return super.containsKey(key);
    }

    private Map<?, ?> getLastNonLeafElement(Object[] pathElements) {
        Map<?, ?> returnValue = this;
        for (int i = 0; i < pathElements.length - 1 && returnValue != null; i++) {
            returnValue = getIfMap(pathElements[i], returnValue);
        }
        return returnValue;
    }

    private int getMaxDepth(Object[] pathElements) {
        Map<?, ?> currentMap = this;
        for (int i = 0; i < pathElements.length - 1; i++) {
            currentMap = getIfMap(pathElements[i], currentMap);
            if (currentMap == null) {
                return i;
            }
        }
        return pathElements.length - 1;
    }

    private boolean containsPath(Object[] pathElements) {
        Map<?, ?> lastNonLeafMap = getLastNonLeafElement(pathElements);
        return lastNonLeafMap != null && lastNonLeafMap.containsKey(getLastPathElement(pathElements));
    }

    @Override
    public Object get(Object key) {
        Object[] pathElements = getPathElements(key);
        if (pathElements != null && pathElements.length > 1) {
            return this.getPath(pathElements);
        }
        return super.get(key);
    }

    private Object getPath(Object[] pathElements) {
        Map<?, ?> lastNonLeafMap = getLastNonLeafElement(pathElements);
        if (lastNonLeafMap != null) {
            return lastNonLeafMap.get(getLastPathElement(pathElements));
        }
        return null;
    }

    @Override
    public Object put(String key, Object value) {
        Object[] pathElements = getPathElements(key);
        if (pathElements != null && pathElements.length > 1) {
            return this.putPath(pathElements, value);
        }
        return super.put(key, value);
    }

    @SuppressWarnings("unchecked")
    private Object putPath(Object[] pathElements, Object value) {
        int maxDepth = getMaxDepth(pathElements);
        Map<?, ?> currentMap = this;
        for (int i = 0; i < maxDepth; i++) {
            currentMap = getIfMap(pathElements[i], currentMap);
        }
        for (int i = maxDepth; i < pathElements.length - 1; i++) {
            Map<?, ?> nextMap = new DiffMap();
            ((Map<Object, Object>) currentMap).put(pathElements[i], nextMap);
            currentMap = nextMap;
        }
        return ((Map<Object, Object>) currentMap).put(getLastPathElement(pathElements), value);
    }

    @Override
    public Object remove(Object key) {
        Object[] pathElements = getPathElements(key);
        if (pathElements != null && pathElements.length > 1) {
            return this.removePath(pathElements);
        }
        return super.remove(key);
    }

    private Object removePath(Object[] pathElements) {
        int maxDepth = getMaxDepth(pathElements);
        Deque<Map<?, ?>> mapStack = new ArrayDeque<>(maxDepth);
        mapStack.push(this);
        for (int i = 0; i < maxDepth; i++) {
            mapStack.push(getIfMap(pathElements[i], mapStack.peek()));
        }
        Map<?, ?> currentMap = mapStack.pop();
        Object returnValue = currentMap.remove(pathElements[maxDepth]);
        for (int i = maxDepth - 1; i >= 0 && currentMap.isEmpty() && !mapStack.isEmpty(); i--) {
            currentMap = mapStack.pop();
            currentMap.remove(pathElements[i]);
        }
        return returnValue;
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        m.forEach(this::put);
    }

    public Map<Object, Object> flatMap() {
        Map<Object, Object> flatMap = new LinkedHashMap<>();
        this.flatKeySet().forEach(k -> flatMap.put(k, this.get(k)));
        return flatMap;
    }

    public Set<Object> flatKeySet() {
        return recursiveKeySet(this);
    }

    private static Set<Object> recursiveKeySet(Map<?, ?> m) {
        return m.keySet().stream()
            .map(k -> recursiveKeySet(k, m))
            .flatMap(List::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<Object> recursiveKeySet(Object k, Map<?, ?> map) {
        Object m = map.get(k);
        if (k instanceof String && m instanceof Map && !((Map) m).isEmpty()) {
            return recursiveKeySet((Map) m).stream()
                .filter(sk -> sk instanceof String)
                .map(sk -> k + "." + sk)
                .collect(Collectors.toList());
        }
        return Collections.singletonList(k);
        //jeevan
        //harika
    }

    public Set<Map.Entry<Object, Object>> flatEntrySet() {
        return this.flatMap().entrySet();
    }

}
