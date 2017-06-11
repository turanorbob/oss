package org.jks.office.util;

import org.apache.poi.util.StringUtil;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/6/11.
 */
public class POIBuilder {
    private Logger logger = Logger.getLogger(POIBuilder.class.getName());
    XWPFDocument doc;
    XWPFTable currentTable;
    Map<String, XWPFTable> tableMap;

    public POIBuilder() {
        doc = new XWPFDocument();
        tableMap = new HashMap<>();
    }

    private void autoWidth(XWPFTable t){
        // 宽度
        CTTblWidth tt = t.getCTTbl().getTblPr().getTblW();
        tt.setW(BigInteger.valueOf(5000));
        tt.setType(STTblWidth.PCT);
        //
    }

    private void createTable(String tableName, Integer rows, Integer cols){
        if(tableName == null || tableName.length() == 0){
            throw new IllegalArgumentException("tableName is required");
        }
        XWPFTable t = null;
        if(rows == null || cols == null){
            t = doc.createTable();
        }
        else{
            t = doc.createTable(rows, cols);
        }
        autoWidth(t);
        currentTable = t;
        tableMap.put(tableName, t);
    }

    public POIBuilder table(String tableName) {
        createTable(tableName, null, null);
        return this;
    }

    public POIBuilder table(String tableName, int rows, int cols) {
        createTable(tableName, rows, cols);
        return this;
    }

    public POIBuilder paragraph(String value){
        XWPFParagraph p1 = doc.createParagraph();
        p1.setAlignment(ParagraphAlignment.LEFT);
        //p1.setBorderBottom(Borders.DOUBLE);
        //p1.setBorderTop(Borders.DOUBLE);

        XWPFRun r1 = p1.createRun();
        r1.setText(value);
        r1.setBold(true);
        r1.setFontFamily("Courier");
        //r1.setUnderline(UnderlinePatterns.DOT_DOT_DASH);
        //r1.setTextPosition(100);
        return this;
    }

    /**
     * @param row   从0开始
     * @param col   从0开始
     * @param value
     * @return
     */
    public POIBuilder cell(int row, int col, String value) {
        int rowNum = currentTable.getNumberOfRows();
        int colNum = currentTable.getRow(0).getTableCells().size();
        if (row >= rowNum) {
            logger.info("add new row");
            for (int i = rowNum; i <= row; i++) {
                XWPFTableRow currentRow = currentTable.insertNewTableRow(i);

                for(int j=0;j<colNum;j++){
                    currentRow.addNewTableCell();
                }
                rowNum++;
            }
        }
        if (col >= colNum) {
            logger.info("add new col");
            for(int i=0; i<rowNum;i++){
                for(int j=colNum;j<=col;j++){
                    currentTable.getRow(i).addNewTableCell();
                }
            }
        }

        currentTable.getRow(row).getCell(col).setText(value);
        return this;
    }

    public void save(String fileName) throws IOException {
        OutputStream out = new FileOutputStream(fileName + ".docx");
        try {
            doc.write(out);
        } finally {
            out.close();
            doc.close();
        }
    }
}
