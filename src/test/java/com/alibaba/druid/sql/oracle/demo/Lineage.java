package com.alibaba.druid.sql.oracle.demo;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectQueryBlock;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectTableReference;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitorAdapter;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGASTVisitorAdapter;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.util.JdbcConstants;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lineage extends TestCase {

    private Map<String, String> aliasFieldMapGL;
    private Map<String, String> aliasTableMapGL;

    public void test_for_demo1() throws Exception {
//        String sql = "select ac.username as acc_user, ro.role_id as role_user from accounts ac join account_roles ro on ac.user_id = ro.user_id;";
//        String sql =  "select innertable.acc_user, innertable.role_user from  \n" +
//                "(select\n" +
//                "    ac.username as acc_user,\n" +
//                "    ro.role_id as role_user\n" +
//                "from accounts ac join account_roles ro on ac.user_id = ro.user_id) as innertable";
        String sql = "select innertable.clearuser from\n" +
                "(select inn.acc_user as clearuser from (select\n" +
                "    ac.username as acc_user,\n" +
                "    ro.role_id as role_user\n" +
                "from accounts ac join account_roles ro on ac.user_id = ro.user_id) as inn) as innertable";
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.POSTGRESQL);

        SQLSelectStatement stmt = (SQLSelectStatement) stmtList.get(0);

        Lineage.ExportTableAliasVisitor visitor = new Lineage.ExportTableAliasVisitor();
        for (SQLStatement stmt1 : stmtList) {
            stmt1.accept(visitor);
        }
//        Map<String, SQLSelectItem> fieldAlias = visitor.getFieldMap();
//        Map<String, SQLTableSource> tableAlias = visitor.getTableMap();
        System.out.println("Fields");
        System.out.println(visitor.getFieldMap());
        System.out.println("Tables");
        System.out.println(visitor.getTableMap());

        aliasFieldMapGL = visitor.getFieldMap();
        aliasTableMapGL = visitor.getTableMap();

//        search("innertable.acc_user");
//        search("innertable.role_user");
        search("innertable.clearuser");

    }

    public void search(String columnName){
        System.out.printf("source is %s \n\r", columnName);
        find(columnName);
    }
    public void find(String columnName){
        String alias = getAlias(columnName);
        String fullPath=aliasFieldMapGL.get(alias);
        if (fullPath != null) {
            System.out.printf("Path %s -> %s \n\r", alias, fullPath);
            find(fullPath);
        }else{
            System.out.printf("target is %s in %s \n\r", alias, aliasTableMapGL.get(getPrefix(columnName)));
        }
    }

    public String getAlias(String columnName){
        int dotPosition = columnName.indexOf('.');
        if (dotPosition > 0) {
            return columnName.substring(dotPosition+1);
        }
        return columnName;
    }

    public String getPrefix(String columnName){
        int dotPosition = columnName.indexOf('.');
        if (dotPosition > 0) {
            return columnName.substring(0, dotPosition);
        }
        return null;
    }



    public static class ExportTableAliasVisitor extends PGASTVisitorAdapter {
//        private Map<String, SQLSelectItem> aliasFieldMap = new HashMap<String, SQLSelectItem>();
//        private Map<String, SQLTableSource> aliasTableMap = new HashMap<String, SQLTableSource>();

        private Map<String, String> aliasFieldMap = new HashMap<String, String>();
        private Map<String, String> aliasTableMap = new HashMap<String, String>();



        public boolean visit(SQLSelectQueryBlock x) {
//            System.out.println(x.getSelectList());
//            fieldsMap.addAll(x.getSelectList());
            return true;
        }

        public boolean visit(SQLSelectItem x) {
            aliasFieldMap.put(x.getAlias(), x.getExpr().toString());
//            x.getExpr().toString()
            return true;
        }

        //SQLSelectItem
        //SQLExprTableSource

        public boolean visit(SQLExprTableSource x) {
            aliasTableMap.put(x.getAlias(),x.getExpr().toString());
//            System.out.println(x.getAlias()+" "+x.computeAlias()+" "+x.getColumns());
//            String alias = x.getAlias();
            return true;
        }

        public boolean visit(SQLSubqueryTableSource x) {
//            aliasTableMap.put(x.getAlias(),x);
            return true;
        }

        public Map<String, String> getFieldMap() {
            return aliasFieldMap;
        }

        public Map<String, String> getTableMap() {
            return aliasTableMap;
        }

//        public Map<String, SQLSelectItem> getFieldMap() {
//            return aliasFieldMap;
//        }
//
//        public Map<String, SQLTableSource> getTableMap() {
//            return aliasTableMap;
//        }
    }
}
