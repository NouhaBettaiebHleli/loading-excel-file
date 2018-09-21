package com.ositel.loadingexcelfile.domain;

import javax.persistence.*;

@Entity
public class ExcelFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String fileName;
    private String filePath;

    public ExcelFile() {
    }

    public ExcelFile(String fileName) {
        this.fileName = fileName;
    }

    public ExcelFile(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
