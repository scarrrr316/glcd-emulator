package com.ibasco.glcdemu.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ListenerOptions {
    private Map<ListenerOption, Object> options = new HashMap<>();

    public ListenerOptions() {
    }

    public <T> ListenerOptions put(ListenerOption<T> option, T value) {
        options.put(option, value);
        return this;
    }

    public <T> T get(ListenerOption<T> option) {
        return (T) options.get(option);
    }

    public Set<ListenerOption> getOptions() {
        return options.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ListenerOption, Object> option : options.entrySet()) {
            sb.append(option.getKey().name());
            sb.append(" = ");
            sb.append(option.getValue());
            sb.append(", ");
        }
        return sb.toString();
    }
}
