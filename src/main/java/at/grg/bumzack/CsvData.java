package at.grg.bumzack;

import java.util.List;

import static java.util.stream.Collectors.joining;

public class CsvData {
    private List<CsvData> data;

    public CsvData(List<CsvData> data) {
        this.data = data;
    }

    public List<CsvData> getData() {
        return data;
    }

    public void setData(List<CsvData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final var collect = data.stream()
                .map(CsvData::toString)
                .collect(joining("\n"));
        return "CsvData{" +
                "data= \n" + collect +
                '}';
    }
}
