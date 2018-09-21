package com.ositel.loadingexcelfile.service;

import com.ositel.loadingexcelfile.domain.ExcelFile;
import com.ositel.loadingexcelfile.domain.ExcelFileRepository;
import com.ositel.loadingexcelfile.domain.dto.ExcelFileUpdateDTO;
import com.ositel.loadingexcelfile.domain.dto.SheetDTO;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;


@Service
public class ExcelFileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelFileService.class);
    public static final String UPLOAD_DIR = "uploads";

    @Autowired
    private ExcelFileRepository excelFileRepository;

    public Long addExcelFile(String fileName) {
        Assert.notNull(fileName, "File Name is required");

        LOGGER.info("Saving excel file with name: '{}'", fileName);

        ExcelFile excelFile = new ExcelFile(fileName);
        excelFileRepository.save(excelFile);

        LOGGER.info("Excel file with name: '{}' saved with id: '{}'", fileName, excelFile.getId());

        return excelFile.getId();
    }

    public boolean uploadExcelFile(Long excelFileId, MultipartFile file) throws IOException {
        Assert.notNull(excelFileId, "File ID is required");
        Assert.notNull(file, "Uploaded file cannot be null");
        Assert.isTrue(file.getBytes().length != 0, "Uploaded file cannot be empty");


        LOGGER.info("Searching for excel file with id: '{}'", excelFileId);
        Optional<ExcelFile> excelFile = excelFileRepository.findById(excelFileId);

        if (!excelFile.isPresent()) {
            LOGGER.error("Excel File with id '" + excelFileId + "' cannot be found");
            throw new IllegalArgumentException("Excel File with id '" + excelFileId + "' cannot be found");
        }

        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR, new Date().getTime() + file.getOriginalFilename());

            if (!Files.exists(path.getParent())) {
                LOGGER.warn("Upload directory is not found, creating under: {}", path.getParent().toAbsolutePath().toString());
                Files.createDirectories(path.getParent());
            }


            LOGGER.info("Saving file under: '{}'", path.toString());
            Files.write(path, bytes);

            excelFile.get().setFilePath(path.toString());

            excelFileRepository.save(excelFile.get());
        } catch (IOException e) {
            LOGGER.error("Could not save file for excel file: '" + excelFileId + "'", e);
            return false;
        }

        return true;
    }


    public SheetDTO searchExcelFile(String fileName) {

        Assert.notNull(fileName, "File name is required");


        ExcelFile excelFile = findExcelFileByFileName(fileName);

        try {
            XSSFSheet sheet = getSheet(excelFile.getFilePath());

            DataFormatter dataFormatter = new DataFormatter();

            SheetDTO sheetDTO = new SheetDTO(fileName);

            LOGGER.info("Reading excel body");

            sheet.forEach(row -> row.forEach(cell -> {
                if (cell.getRowIndex() == 0) {
                    sheetDTO.getHeaderColumn().add(dataFormatter.formatCellValue(cell));
                } else {
                    if (!sheetDTO.getLinesValue().containsKey(cell.getRowIndex())) {
                        sheetDTO.getLinesValue().put(cell.getRowIndex(), new ArrayList<>());
                    }
                    sheetDTO.getLinesValue().get(cell.getRowIndex()).add(dataFormatter.formatCellValue(cell));
                }
            }));

            sheet.getWorkbook().close();

            return sheetDTO;
        } catch (IOException e) {
            LOGGER.error("An error has occurred while reading the excel file", e);
        }

        return null;
    }

    public SheetDTO updateCellValue(ExcelFileUpdateDTO excelFileUpdateDTO, Integer col, Integer row) {

        Assert.notNull(excelFileUpdateDTO, "File update content is required");
        Assert.notNull(excelFileUpdateDTO.getFileName(), "File name is required");
        Assert.notNull(excelFileUpdateDTO.getValue(), "The new value is required");
        Assert.isTrue(col >= 0, "Column number must be greater or equal to 0");
        Assert.isTrue(row >= 0, "Row number must be greater or equal to 0");

        ExcelFile excelFile = findExcelFileByFileName(excelFileUpdateDTO.getFileName());

        XSSFSheet sheet = getSheet(excelFile.getFilePath());

        XSSFRow sheetRow = sheet.getRow(row);
        if (sheetRow == null) {
            LOGGER.error("Row '{}' was not found in the sheet", row);
            throw new IllegalStateException("Row could not be found");
        }


        XSSFCell sheetCell = sheetRow.getCell(col - 1);
        if (sheetCell == null) {
            LOGGER.error("Column '{}' was not found in the sheet", row);
            throw new IllegalStateException("Column could not be found");
        }

        LOGGER.info("Updating value for row: {}, col: {}, old value: {}, new value: {}",
                row, col, sheetCell.getRawValue(), excelFileUpdateDTO.getValue());

        sheetCell.setCellValue(excelFileUpdateDTO.getValue());

        try {
            LOGGER.info("Saving the updated file: {}", excelFile.getFilePath());
            FileOutputStream fileOutputStream = new FileOutputStream(new File(excelFile.getFilePath()));
            sheet.getWorkbook().write(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            LOGGER.error("An error has occurred while saving the excel file", e);
        }

        LOGGER.info("Re-reading the updated file: {}", excelFile.getFilePath());
        return searchExcelFile(excelFile.getFileName());
    }

    private ExcelFile findExcelFileByFileName(String fileName) {
        LOGGER.info("Finding excel by file name: {}", fileName);
        Optional<ExcelFile> excelFile = excelFileRepository.findByFileName(fileName);

        if (!excelFile.isPresent()) {
            LOGGER.error("Excel File with name '" + fileName + "' cannot be found");
            throw new IllegalArgumentException("Excel File with name '" + fileName + "' cannot be found");
        }

        return excelFile.get();
    }

    private XSSFSheet getSheet(String filePath) {
        LOGGER.info("Opening excel file under: {}", filePath);

        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalStateException("The excel contains no sheets: " + workbook.getNumberOfSheets());
            } else if (workbook.getNumberOfSheets() > 1) {
                LOGGER.warn("The excel file contains more the one sheet, the first sheet will be used.");
            }

            return workbook.getSheetAt(0);
        } catch (IOException e) {
            LOGGER.error("Could not open excel file: " + filePath, e);
            throw new IllegalStateException("Could not open excel file");
        }
    }
}
