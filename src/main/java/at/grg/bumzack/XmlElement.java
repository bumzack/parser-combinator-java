package at.grg.bumzack;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Objects;

public class XmlElement {
    private String name;
    private List<Pair<String, String>> attributes;
    private List<XmlElement> children;

    public XmlElement() {
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

    public List<XmlElement> getChildren() {
        return children;
    }

    public void setChildren(List<XmlElement> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "XmlElement{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                ", children=" + children +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XmlElement that = (XmlElement) o;
        return Objects.equals(name, that.name) && Objects.equals(attributes, that.attributes) && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, attributes, children);
    }
}
