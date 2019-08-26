package com.webank.webase.front.util;

import com.webank.webase.front.base.exception.FrontException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.precompile.crud.EnumOP;
import org.fisco.bcos.web3j.precompile.crud.Table;

import java.util.*;

public class CRUDParseUtils {

    public static final String PRIMARY_KEY = "primary key";

    public static void parseCreateTable(String sql, Table table)
            throws JSQLParserException, FrontException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        CreateTable createTable = (CreateTable) statement;

        // parse table name
        String tableName = createTable.getTable().getName();
        table.setTableName(tableName);

        // parse key from index
        boolean keyFlag = false;
        List<Index> indexes = createTable.getIndexes();
        if (indexes != null) {
            if (indexes.size() > 1) {
                throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                        "Please provide only one primary key for the table.");
            }
            keyFlag = true;
            Index index = indexes.get(0);
            String type = index.getType().toLowerCase();
            if (PRIMARY_KEY.equals(type)) {
                table.setKey(index.getColumnsNames().get(0));
            } else {
                throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                        "Please provide only one primary key for the table.");
            }
        }
        List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
        // parse key from ColumnDefinition
        for (int i = 0; i < columnDefinitions.size(); i++) {
            List<String> columnSpecStrings = columnDefinitions.get(i).getColumnSpecStrings();
            if (columnSpecStrings == null) {
                continue;
            } else {
                if (columnSpecStrings.size() == 2
                        && "primary".equals(columnSpecStrings.get(0))
                        && "key".equals(columnSpecStrings.get(1))) {
                    String key = columnDefinitions.get(i).getColumnName();
                    if (keyFlag) {
                        if (!table.getKey().equals(key)) {
                            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                                    "Please provide only one primary key for the table.");
                        }
                    } else {
                        keyFlag = true;
                        table.setKey(key);
                    }
                    break;
                }
            }
        }
        if (!keyFlag) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "Please provide a primary key for the table.");
        }
        // parse value field
        List<String> fieldsList = new ArrayList<>();
        for (int i = 0; i < columnDefinitions.size(); i++) {
            String columnName = columnDefinitions.get(i).getColumnName();
            if (fieldsList.contains(columnName)) {
                throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                        "Please provide the field '" + columnName + "' only once.");
            } else {
                fieldsList.add(columnName);
            }
        }
        if (!fieldsList.contains(table.getKey())) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Please provide the field '" + table.getKey() + "' in column definition.");
        } else {
            fieldsList.remove(table.getKey());
        }
        StringBuffer fields = new StringBuffer();
        for (int i = 0; i < fieldsList.size(); i++) {
            fields.append(fieldsList.get(i));
            if (i != fieldsList.size() - 1) {
                fields.append(",");
            }
        }
        table.setValueFields(fields.toString());
    }

    public static boolean parseInsert(String sql, Table table, Entry entry)
            throws JSQLParserException, FrontException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insert = (Insert) statement;

        if (insert.getSelect() != null) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The insert select clause is not supported.");
        }
        // parse table name
        String tableName = insert.getTable().getName();
        table.setTableName(tableName);

        // parse columns
        List<Column> columns = insert.getColumns();
        ItemsList itemsList = insert.getItemsList();
        String items = itemsList.toString();
        String[] rawItem = items.substring(1, items.length() - 1).split(",");
        String[] itemArr = new String[rawItem.length];
        for (int i = 0; i < rawItem.length; i++) {
            itemArr[i] = rawItem[i].trim();
        }
        if (columns != null) {
            if (columns.size() != itemArr.length) {
                throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "Column count doesn't match value count.");
            }
            List<String> columnNames = new ArrayList<>();
            for (Column column : columns) {
                String columnName = trimQuotes(column.toString());
                if (columnNames.contains(columnName)) {
                    throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                            "Please provide the field '" + columnName + "' only once.");
                } else {
                    columnNames.add(columnName);
                }
            }
            for (int i = 0; i < columnNames.size(); i++) {
                entry.put(columnNames.get(i), trimQuotes(itemArr[i]));
            }
            return false;
        } else {
            for (int i = 0; i < itemArr.length; i++) {
                entry.put(i + "", trimQuotes(itemArr[i]));
            }
            return true;
        }
    }

    public static void parseSelect(
            String sql, Table table, Condition condition, List<String> selectColumns)
            throws JSQLParserException, FrontException {
        Statement statement;
        statement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) statement;

        // parse table name
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        if (tableList.size() != 1) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "Please provide only one table name.");
        }
        table.setTableName(tableList.get(0));

        // parse where clause
        PlainSelect selectBody = (PlainSelect) selectStatement.getSelectBody();
        if (selectBody.getOrderByElements() != null) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The order clause is not supported.");
        }
        if (selectBody.getGroupBy() != null) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The group clause is not supported.");
        }
        if (selectBody.getHaving() != null) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The having clause is not supported.");
        }
        if (selectBody.getJoins() != null) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The join clause is not supported.");
        }
        if (selectBody.getTop() != null) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The top clause is not supported.");
        }
        if (selectBody.getDistinct() != null) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The distinct clause is not supported.");
        }
        Expression expr = selectBody.getWhere();
        condition = handleExpression(condition, expr);

        Limit limit = selectBody.getLimit();
        if (limit != null) {
            parseLimit(condition, limit);
        }

        // parse select item
        List<SelectItem> selectItems = selectBody.getSelectItems();
        for (SelectItem item : selectItems) {
            if (item instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) item;
                Expression expression = selectExpressionItem.getExpression();
                if (expression instanceof Function) {
                    Function func = (Function) expression;
                    throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                            "The " + func.getName() + " function is not supported.");
                }
            }
            selectColumns.add(item.toString());
        }
    }

    private static Condition handleExpression(Condition condition, Expression expr)
            throws FrontException {
        if (expr instanceof BinaryExpression) {
            condition = getWhereClause((BinaryExpression) (expr), condition);
        }
        if (expr instanceof OrExpression) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The OrExpression is not supported.");
        }
        if (expr instanceof NotExpression) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The NotExpression is not supported.");
        }
        if (expr instanceof InExpression) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The InExpression is not supported.");
        }
        if (expr instanceof LikeExpression) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The LikeExpression is not supported.");
        }
        if (expr instanceof SubSelect) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The SubSelect is not supported.");
        }
        if (expr instanceof IsNullExpression) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "The IsNullExpression is not supported.");
        }
        Map<String, Map<EnumOP, String>> conditions = condition.getConditions();
        Set<String> keys = conditions.keySet();
        for (String key : keys) {
            Map<EnumOP, String> value = conditions.get(key);
            EnumOP operation = value.keySet().iterator().next();
            String itemValue = value.values().iterator().next();
            String newValue = trimQuotes(itemValue);
            value.put(operation, newValue);
            conditions.put(key, value);
        }
        condition.setConditions(conditions);
        return condition;
    }

    public static String trimQuotes(String str) {
        char[] value = str.toCharArray();
        int len = value.length;
        int st = 1;
        char[] val = value; /* avoid getfield opcode */

        while ((st < len) && (val[st] == '"' || val[st] == '\'')) {
            st++;
        }
        while ((st < len) && (val[len - 1] == '"' || val[len - 1] == '\'')) {
            len--;
        }
        String string = ((st > 1) || (len < value.length)) ? str.substring(st, len) : str;
        return string;
    }

    public static void parseUpdate(String sql, Table table, Entry entry, Condition condition)
            throws JSQLParserException, FrontException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Update update = (Update) statement;

        // parse table name
        List<net.sf.jsqlparser.schema.Table> tables = update.getTables();
        String tableName = tables.get(0).getName();
        table.setTableName(tableName);

        // parse cloumns
        List<Column> columns = update.getColumns();
        List<Expression> expressions = update.getExpressions();
        int size = expressions.size();
        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = expressions.get(i).toString();
        }
        for (int i = 0; i < columns.size(); i++) {
            entry.put(trimQuotes(columns.get(i).toString()), trimQuotes(values[i]));
        }

        // parse where clause
        Expression where = update.getWhere();
        if (where != null) {
            BinaryExpression expr2 = (BinaryExpression) (where);
            handleExpression(condition, expr2);
        }
        Limit limit = update.getLimit();
        parseLimit(condition, limit);
    }

    public static void parseRemove(String sql, Table table, Condition condition)
            throws JSQLParserException, FrontException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Delete delete = (Delete) statement;

        // parse table name
        net.sf.jsqlparser.schema.Table sqlTable = delete.getTable();
        table.setTableName(sqlTable.getName());

        // parse where clause
        Expression where = delete.getWhere();
        if (where != null) {
            BinaryExpression expr = (BinaryExpression) (where);
            handleExpression(condition, expr);
        }
        Limit limit = delete.getLimit();
        parseLimit(condition, limit);
    }

    private static void parseLimit(Condition condition, Limit limit)
            throws FrontException {
        if (limit != null) {
            Expression offset = limit.getOffset();
            Expression count = limit.getRowCount();
            try {
                if (offset != null) {
                    condition.Limit(
                            Integer.parseInt(offset.toString()),
                            Integer.parseInt(count.toString()));
                } else {
                    condition.Limit(Integer.parseInt(count.toString()));
                }
            } catch (NumberFormatException e) {
                throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                        "Please provide limit parameters by non-negative integer mode, "
                                + PrecompiledUtils.NonNegativeIntegerRange
                                + ".");
            }
        }
    }

    private static Condition getWhereClause(Expression expr, Condition condition) {

        expr.accept(
                new ExpressionVisitorAdapter() {

                    @Override
                    protected void visitBinaryExpression(BinaryExpression expr) {
                        if (expr instanceof ComparisonOperator) {
                            String key = trimQuotes(expr.getLeftExpression().toString());
                            String operation = expr.getStringExpression();
                            String value = trimQuotes(expr.getRightExpression().toString());
                            switch (operation) {
                                case "=":
                                    condition.EQ(key, value);
                                    break;
                                case "!=":
                                    condition.NE(key, value);
                                    break;
                                case ">":
                                    condition.GT(key, value);
                                    break;
                                case ">=":
                                    condition.GE(key, value);
                                    break;
                                case "<":
                                    condition.LT(key, value);
                                    break;
                                case "<=":
                                    condition.LE(key, value);
                                    break;
                                default:
                                    break;
                            }
                        }
                        super.visitBinaryExpression(expr);
                    }
                });
        return condition;
    }

    // change exception to return error message
    public static String invalidSymbolReturn(String sql)  {
        if (sql.contains("；")) {
            return "SyntaxError: Unexpected Chinese semicolon.";
        } else if (sql.contains("“")
                || sql.contains("”")
                || sql.contains("‘")
                || sql.contains("’")) {
            return "SyntaxError: Unexpected Chinese quotes.";
        } else if (sql.contains("，")) {
            return "SyntaxError: Unexpected Chinese comma.";
        } else {
            return "SyntaxError: Please check syntax";
        }
    }
    public static void invalidSymbol(String sql) throws FrontException {
        if (sql.contains("；")) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "SyntaxError: Unexpected Chinese semicolon.");
        } else if (sql.contains("“")
                || sql.contains("”")
                || sql.contains("‘")
                || sql.contains("’")) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "SyntaxError: Unexpected Chinese quotes.");
        } else if (sql.contains("，")) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR, "SyntaxError: Unexpected Chinese comma.");
        }
    }

    public static void checkTableParams(Table table) throws FrontException {
        if (table.getTableName().length() > PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                    "The table name length is greater than "
                            + PrecompiledUtils.SYS_TABLE_KEY_MAX_LENGTH
                            + ".");
        }
        if (table.getKey().length() > PrecompiledUtils.SYS_TABLE_KEY_FIELD_NAME_MAX_LENGTH) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                    "The table primary key name length is greater than "
                            + PrecompiledUtils.SYS_TABLE_KEY_FIELD_NAME_MAX_LENGTH
                            + ".");
        }
        String[] valueFields = table.getValueFields().split(",");
        for (String valueField : valueFields) {
            if (valueField.length() > PrecompiledUtils.USER_TABLE_FIELD_NAME_MAX_LENGTH) {
                throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                        "The table field name length is greater than "
                                + PrecompiledUtils.USER_TABLE_FIELD_NAME_MAX_LENGTH
                                + ".");
            }
        }
        if (table.getValueFields().length() > PrecompiledUtils.SYS_TABLE_VALUE_FIELD_MAX_LENGTH) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                    "The table total field name length is greater than "
                            + PrecompiledUtils.SYS_TABLE_VALUE_FIELD_MAX_LENGTH
                            + ".");
        }
    }

    public static void checkUserTableParam(Entry entry, Table descTable)
            throws FrontException {
        Map<String, String> fieldsMap = entry.getFields();
        Set<String> keys = fieldsMap.keySet();
        for (String key : keys) {
            if (key.equals(descTable.getKey())) {
                if (fieldsMap.get(key).length() > PrecompiledUtils.USER_TABLE_KEY_VALUE_MAX_LENGTH) {
                    throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                            "The table primary key value length is greater than "
                                    + PrecompiledUtils.USER_TABLE_KEY_VALUE_MAX_LENGTH
                                    + ".");
                }
            } else {
                if (fieldsMap.get(key).length() > PrecompiledUtils.USER_TABLE_FIELD_VALUE_MAX_LENGTH) {
                    throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                            "The table field '" + key + "' value length is greater than 16M - 1.");
                }
            }
        }
    }
    public static void handleKey(Table table, Condition condition) throws Exception {

        String keyName = table.getKey();
        String keyValue = "";
        Map<EnumOP, String> keyMap = condition.getConditions().get(keyName);
        if (keyMap == null) {
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Please provide a equal condition for the key field '"
                            + keyName
                            + "' in where clause.");
        } else {
            Set<EnumOP> keySet = keyMap.keySet();
            for (EnumOP enumOP : keySet) {
                if (enumOP != EnumOP.eq) {
                    throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                            "Please provide a equal condition for the key field '"
                                    + keyName
                                    + "' in where clause.");
                } else {
                    keyValue = keyMap.get(enumOP);
                }
            }
        }
        table.setKey(keyValue);
    }

    public static List<Map<String, String>> filterSystemColum(List<Map<String, String>> result) {

        List<String> filteredColumns = Arrays.asList("_id_", "_hash_", "_status_", "_num_");
        List<Map<String, String>> filteredResult = new ArrayList<>(result.size());
        Map<String, String> filteredRecords;
        for (Map<String, String> records : result) {
            filteredRecords = new LinkedHashMap<>();
            Set<String> recordKeys = records.keySet();
            for (String recordKey : recordKeys) {
                if (!filteredColumns.contains(recordKey)) {
                    filteredRecords.put(recordKey, records.get(recordKey));
                }
            }
            filteredResult.add(filteredRecords);
        }
        return filteredResult;
    }

    public static List<Map<String, String>> getSeletedColumn(
            List<String> selectColumns, List<Map<String, String>> result) {
        List<Map<String, String>> selectedResult = new ArrayList<>(result.size());
        Map<String, String> selectedRecords;
        for (Map<String, String> records : result) {
            selectedRecords = new LinkedHashMap<>();
            for (String column : selectColumns) {
                Set<String> recordKeys = records.keySet();
                for (String recordKey : recordKeys) {
                    if (recordKey.equals(column)) {
                        selectedRecords.put(recordKey, records.get(recordKey));
                    }
                }
            }
            selectedResult.add(selectedRecords);
        }
        selectedResult.stream().forEach(System.out::println);
        return selectedResult;
    }

}
