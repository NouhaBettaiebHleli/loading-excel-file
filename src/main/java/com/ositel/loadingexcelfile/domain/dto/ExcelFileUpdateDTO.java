package com.ositel.loadingexcelfile.domain.dto;

public class ExcelFileUpdateDTO {
    private String fileName;
    private String value;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
