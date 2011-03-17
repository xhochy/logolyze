package de.logotakt.logolyze.view.swing2d;

import org.apache.commons.collections15.Transformer;

import de.logotakt.logolyze.model.interfaces.INode;

/**
 * This is the transformer used by JUNG to map Nodes to Strings (i.e. their labels).
 * Instead of just returning their label strings, it tries not to let the labels grow too big.
 */
final class LabelNameTransformer implements Transformer<INode, String> {
    // Label length in characters from which to split the line.
    private static final int MAX_LABEL_LENGTH = 15;

    /**
     * This returns the labeling String to be displayed. For too long labels, it will insert newlines
     * to make the labels more compact.
     * @return the labeling String to be displayed
     */
    public String transform(final INode node) {
        String label = node.getLabel();
        if (label.length() <= LabelNameTransformer.MAX_LABEL_LENGTH) {
            return label;
        } else {
            int middle = label.length() / 2;
            int spaceBefore = label.lastIndexOf(' ', middle);
            spaceBefore = spaceBefore == -1 ? 0 : spaceBefore;
            int spaceAfter = label.indexOf(' ', middle);
            spaceAfter = spaceAfter == -1 ? label.length() : spaceAfter;
            int target;
            if (middle - spaceBefore <= spaceAfter - middle) {
                target = spaceBefore;
            } else {
                target = spaceAfter;
            }

            return String.format("<html> %s <br /> %s </html>", label.substring(0, target), label.substring(target));
        }
    }
}
