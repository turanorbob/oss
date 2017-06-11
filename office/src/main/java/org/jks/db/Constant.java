package org.jks.db;

/**
 * Created by Administrator on 2017/6/11.
 */
public class Constant {

    public static final StringBuffer TABLE_INFO = new StringBuffer();
    public static final StringBuffer COLUMN_INFO = new StringBuffer();

    public static final String COLUMN_FIELD_NAME = "属性名";
    public static final String COLUMN_FIELD_TYPE = "字段类型";
    public static final String COLUMN_FIELD_DESCRIPTION = "描述";

    static {
        TABLE_INFO.append("select tablename as tableName, obj_description(pg_class.oid) as description from pg_tables ")
                .append("left join pg_class on tablename=relname ")
                .append("where schemaname='public'");
        COLUMN_INFO.append("SELECT c.table_schema as tableSchema,c.table_name as tableName," )
                .append("c.column_name as ColumnName, ")
                .append("case when c.udt_name='varchar' then c.udt_name||'('||c.character_maximum_length||')'")
                .append("when c.udt_name='numeric' then c.udt_name||'('||c.numeric_precision||')'")
                .append("else c.udt_name end as dataType, ")
                .append("pgd.description as description ")
                .append("FROM pg_catalog.pg_statio_all_tables as st ")
                .append("inner join pg_catalog.pg_description pgd on (pgd.objoid=st.relid) ")
                .append("inner join information_schema.columns c on (pgd.objsubid=c.ordinal_position ")
                .append("and  c.table_schema=st.schemaname and c.table_name=st.relname) where c.table_name='%s'");
    }
}
