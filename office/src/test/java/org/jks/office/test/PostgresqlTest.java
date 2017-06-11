package org.jks.office.test;

import com.google.common.collect.Maps;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.jks.db.ColumeModel;
import org.jks.db.Constant;
import org.jks.db.TableModel;
import org.jks.office.util.POIBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/11.
 */
@Log
public class PostgresqlTest {

    public static void main(String args[]) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException, IOException {
        String url = "jdbc:postgresql://localhost:5432/linktruck" ;
        Connection con = DriverManager.getConnection(url, "postgres" , "password" );

        ResultSetHandler<List<TableModel>> tHandler = new BeanListHandler<TableModel>(TableModel.class);
        ResultSetHandler<List<ColumeModel>> cHandler  = new BeanListHandler<ColumeModel>(ColumeModel.class);

        QueryRunner run = new QueryRunner();
        List<TableModel> tableModels = run.query(con, Constant.TABLE_INFO.toString(),tHandler);
        Map<String, TableModel> tableModelMap = Maps.newHashMap();
        if(!CollectionUtils.isEmpty(tableModels)){
            tableModels.forEach(tableModel -> {
                tableModelMap.put(tableModel.getTableName(), tableModel);
            });
            for (TableModel tableModel : tableModels) {
                String sql = String.format(Constant.COLUMN_INFO.toString(), tableModel.getTableName());
                List<ColumeModel> columeModels = run.query(con, sql, cHandler);
                tableModelMap.get(tableModel.getTableName()).setColumeModelList(columeModels);
            }
            POIBuilder builder = new POIBuilder();
            tableModels.forEach(tableModel -> {
                builder.paragraph(tableModel.getDescription()+"("+tableModel.getTableName()+")");
                builder.table(tableModel.getTableName(), tableModel.getColumeModelList().size()+1,3);
                builder.cell(0,0,Constant.COLUMN_FIELD_NAME)
                        .cell(0,1,Constant.COLUMN_FIELD_TYPE)
                        .cell(0,2,Constant.COLUMN_FIELD_DESCRIPTION);
                int i=0;
                for (ColumeModel columeModel : tableModel.getColumeModelList()) {
                    builder.cell(i, 0, columeModel.getColumnName())
                            .cell(i, 1, columeModel.getDataType())
                            .cell(i, 2, columeModel.getDescription());
                    i++;
                }
            });
            builder.save("db");
        }
        con.close();
    }
}
