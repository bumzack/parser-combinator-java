package at.grg.bumzack;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Element {
    private String name;
    private List<Pair<String, String>> attributes;
    private List<Element> children;

    public Element() {
    }

    public Element(String name, List<Pair<String, String>> attributes, List<Element> children) {
        this.name = name;
        this.attributes = attributes;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Pair<String, String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Pair<String, String>> attributes) {
        this.attributes = attributes;
    }

    public List<Element> getChildren() {
        return children;
    }

    public void setChildren(List<Element> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                ", children=" + children +
                '}';
    }
}
