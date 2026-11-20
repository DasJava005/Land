package org.github.DasJava.land;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public record LandMotd(List<String> lines, boolean visible) {

    public LandMotd(List<String> lines, boolean visible) {
        this.lines = List.copyOf(lines);
        this.visible = visible;
    }

    public Editor editor() {
        return new Editor(lines, visible);
    }

    public void showRaw(Player p) {
        lines.forEach(p::sendMessage);
    }

    public boolean isEmpty() {
        return this.lines.isEmpty();
    }

    public static class Editor {
        private final List<String> lines;
        private boolean visible;

        public Editor(List<String> lines, boolean visible) {
            this.lines = new ArrayList<>(lines);
            this.visible = visible;
        }

        public Editor setVisibility(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Editor addLine(String line) {
            lines.add(line);
            return this;
        }

        public Editor addLines(List<String> lines) {
            this.lines.addAll(lines);
            return this;
        }

        public Editor setLine(int line, String text) {
            if (line < 0) {
                throw new IllegalArgumentException("Line cannot be negative");
            }
            while(this.lines.size() <= line) {
                this.lines.add("");
            }
            this.lines.set(line, text);
            return this;
        }

        public LandMotd build() {
            return new LandMotd(lines, visible);
        }

    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof LandMotd other)) return false;
        return other.lines.equals(this.lines);
    }

    @Override
    public int hashCode() {
        return lines.hashCode();
    }

    public static LandMotd emptyMotd() {
        return new LandMotd(List.of(), false);
    }

}
