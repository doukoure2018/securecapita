package io.getarrayus.securecapita.utils;


import io.getarrayus.securecapita.entity.ImportFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class ExportConfig {
//
//    private int sheetIndex;
//
//    private int startRow;
//
//    private Class dataClazz;
//
//    private List<CellConfig> cellExportConfigList;
//
//
//    public static final ExportConfig customerExport;
//    static{
//        customerExport = new ExportConfig();
//        customerExport.setSheetIndex(0);
//        customerExport.setStartRow(1);
//        customerExport.setDataClazz(ImportFile.class);
//        List<CellConfig> customerCellConfig = new ArrayList<>();
//        customerCellConfig.add(new CellConfig(0, "nom"));
//        customerCellConfig.add(new CellConfig(1, "prenom"));
//        customerCellConfig.add(new CellConfig(2, "region"));
//        customerCellConfig.add(new CellConfig(3, "prefecture"));
//        customerCellConfig.add(new CellConfig(4, "sousPerfecture"));
//        customerCellConfig.add(new CellConfig(5, "quartierDistrict"));
//        customerCellConfig.add(new CellConfig(6, "contact"));
//        customerCellConfig.add(new CellConfig(7, "message"));
//
//        customerExport.setCellExportConfigList(customerCellConfig);
//    }
//
//}