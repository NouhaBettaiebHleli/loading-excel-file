package com.ositel.loadingexcelfile.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExcelFileRepository extends JpaRepository<ExcelFile, Long> {
    Optional<ExcelFile> findByFileName(String fileName);
}
