package net.trilogy.arch.services;

import java.util.Collection;

import com.google.common.annotations.VisibleForTesting;

import net.trilogy.arch.domain.diff.Diff;
import net.trilogy.arch.domain.diff.DiffableEntity;
import net.trilogy.arch.domain.diff.DiffableRelationship;

public class DiffToDotCalculator {

    public static String toDot(String title, Collection<Diff> diffs) {
        final var dot = new Dot();
        dot.add(0, "digraph " + title + " {");
        dot.add(1, "graph [rankdir=LR];");
        dot.add(0, "");
        diffs.stream()
                .map(d -> toDot(d))
                .forEach(line -> dot.add(1, line));
        dot.add(0, "}");
        return dot.toString();
    }

    private static class Dot {
        private final StringBuilder builder = new StringBuilder();

        public String toString() {
            return builder.toString();
        }

        public void add(int indentationLevel, String line) {
            builder.append("    ".repeat(indentationLevel)).append(line).append("\n");
        }
    }

    @VisibleForTesting
    static String getDotShape(DiffableEntity entity) {
        return "Mrecord";
    }

    @VisibleForTesting
    static String getDotColor(Diff diff) {
        switch (diff.getStatus()) {
            case CREATED:
                return "darkgreen";
            case DELETED:
                return "red";
            case UPDATED:
                return "blue";
            case NO_UPDATE_BUT_CHILDREN_UPDATED:
                return "blueviolet";
            case NO_UPDATE:
                return "black";
            default:
                return "black";
        }
    }

    @VisibleForTesting
    static String toDot(Diff diff) {
        if (diff.getElement() instanceof DiffableEntity) {
            final var entity = (DiffableEntity) diff.getElement();
            return "\"" + entity.getId() + "\" " +
                    "[label=\"" + entity.getName() + 
                    
                    // TODO: Temporary, until shapes are added
                    " | " + entity.getType() + 

                    "\", color=" + getDotColor(diff) +
                    ", fontcolor=" + getDotColor(diff) +
                    ", shape=" + getDotShape(entity) +
                    "];";
        }
        final var relationship = (DiffableRelationship) diff.getElement();
        return "\"" + 
                relationship.getSourceId() + "\" -> \"" + relationship.getRelationship().getWithId() +
                "\" " +
                "[label=\"" + relationship.getName() +
                "\", color=" + getDotColor(diff) +
                ", fontcolor=" + getDotColor(diff) +
                "];";
    }
}