package org.jks.db;

import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/6/11.
 */
@Data
public class TableModel {
    private String tableName;
    private String description;
    private List<ColumeModel> columeModelList;
}
