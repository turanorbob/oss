package org.jks.db;

import lombok.Data;

/**
 * Created by Administrator on 2017/6/11.
 */
@Data
public class ColumeModel {
    String tableSchema;
    String tableName;
    String columnName;
    String description;
    String dataType;
}
