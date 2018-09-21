package com.ositel.loadingexcelfile.domain.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheetDTO {
    private String fileName;
    private List<String> headerColumn;
    private Map<Integer, List<String>> linesValue;

    public SheetDTO() {
        headerColumn = new ArrayList<>();
        linesValue = new HashMap<>();
    }

    public SheetDTO(String fileName) {
        this();
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getHeaderColumn() {
        return headerColumn;
    }

    public void setHeaderColumn(List<String> headerColumn) {
        this.headerColumn = headerColumn;
    }

    public Map<Integer, List<String>> getLinesValue() {
        return linesValue;
    }

    public void setLinesValue(Map<Integer, List<String>> linesValue) {
        this.linesValue = linesValue;
    }
}
