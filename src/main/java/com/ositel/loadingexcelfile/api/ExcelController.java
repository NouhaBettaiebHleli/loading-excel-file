package com.ositel.loadingexcelfile.api;

import com.ositel.loadingexcelfile.domain.dto.ExcelFileDTO;
import com.ositel.loadingexcelfile.domain.dto.ExcelFileUpdateDTO;
import com.ositel.loadingexcelfile.domain.dto.SheetDTO;
import com.ositel.loadingexcelfile.service.ExcelFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RestController
@RequestMapping("api/ositel")
public class ExcelController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelController.class);

    @Autowired
    private ExcelFileService excelFileService;

    @PostMapping("addExcelFile")
    public Long addExcelFile(@RequestBody ExcelFileDTO excelFileDTO) {
        LOGGER.info("REST request to add an excel file");
        if (excelFileDTO == null) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
        return excelFileService.addExcelFile(excelFileDTO.getFileName());
    }

    @PostMapping("{id}/uploadExcelFile")
    public ResponseEntity uploadExcelFile(@PathVariable Long id, /*@RequestParam("file")*/ MultipartFile file) throws IOException {
        LOGGER.info("REST request to upload an excel file with id: {}", id);

        boolean result = excelFileService.uploadExcelFile(id, file);

        return result ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("searchExcelFile")
    public SheetDTO searchExcelFile(@RequestBody ExcelFileDTO excelFileDTO) {
        LOGGER.info("REST request read an excel file");
        return excelFileService.searchExcelFile(excelFileDTO.getFileName());
    }

    @PutMapping("{colonne}/{line}/updateCellValue")
    public SheetDTO updateCellValue(@RequestBody ExcelFileUpdateDTO excelFileUpdateDTO,
                                    @PathVariable("colonne") Integer col,
                                    @PathVariable("line") Integer row) {
        if (excelFileUpdateDTO != null) {
            LOGGER.info("REST request to update excel file for row '{}', col '{}'. File: '{}', new value: {}",
                    row, col, excelFileUpdateDTO.getFileName(), excelFileUpdateDTO.getValue());
        }

        return excelFileService.updateCellValue(excelFileUpdateDTO, col, row);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public void handleBadRequests(HttpServletResponse response, Exception e) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
}
