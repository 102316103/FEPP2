package com.syscom.fep.mybatis.interceptor;

import com.syscom.fep.common.log.LogHelperFactory;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoggerInterceptorTest {
    private final List<String> maskFieldNames = Arrays.asList("SMS_SERVICENAME", "SMS_HOSTNAME", "SMS_THREADS_ACTIVE");
    private final List<String> selectColumnValues = Arrays.asList("SERVICENAME", "SERVICEIP", "HOSTNAME", "THREADS_ACTIVE1", "THREADS_ACTIVE2", "THREADS_ACTIVE3");
    private final List<String> insertColumnValues = Arrays.asList("SERVICENAME", "SERVICEIP", "HOSTNAME", "UPDATETIME", "SERVICESTATE", "STARTTIME", "PID", "CPU", "CPU_THRESHOLD", "RAM", "RAM_THRESHOLD", "THREADS", "THREADS_ACTIVE", "THREADS_THRESHOLD", "STOPTIME", "OTHERS");
    private final List<String> updateColumnValues = Arrays.asList("fepap1", "2024-03-15 09:41:09.717", "1", "2024-03-15 09:40:34.0", "null", "0", "0", "295295", "0", "79", "0", "0", "null", "FEP-SERVICE-APPMON", "127.0.0.1");

    @Test
    public void testParseSelect() throws JSQLParserException {
        List<Integer> maskFieldIndexes = new ArrayList<>();
        String sql = "select SMS_SERVICENAME, SMS_SERVICEIP, SMS_HOSTNAME, SMS_UPDATETIME, SMS_SERVICESTATE, SMS_STARTTIME, SMS_PID, SMS_CPU, SMS_CPU_THRESHOLD, SMS_RAM, SMS_RAM_THRESHOLD, SMS_THREADS, SMS_THREADS_ACTIVE, SMS_THREADS_THRESHOLD, SMS_STOPTIME , SMS_OTHERS from SMS where SMS_SERVICENAME = ? and SMS_SERVICEIP = ? or SMS_HOSTNAME = ? and SMS_THREADS_ACTIVE in (?, ?, ?)";
        Select stmt = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect selectBody = (PlainSelect) stmt.getSelectBody();
        selectBody.getWhere().accept(new WhereAdapter(maskFieldIndexes));
        sql = replaceColumnValue(maskFieldIndexes, selectColumnValues, sql);
        System.out.println(sql);
    }

    @Test
    public void testParseInsert() throws JSQLParserException {
        List<Integer> maskFieldIndexes = new ArrayList<>();
        String sql = "insert into SMS (SMS_SERVICENAME, SMS_SERVICEIP, SMS_HOSTNAME, SMS_UPDATETIME, SMS_SERVICESTATE, SMS_STARTTIME, SMS_PID, SMS_CPU, SMS_CPU_THRESHOLD, SMS_RAM, SMS_RAM_THRESHOLD, SMS_THREADS, SMS_THREADS_ACTIVE, SMS_THREADS_THRESHOLD, SMS_STOPTIME, SMS_OTHERS) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Insert stmt = (Insert) CCJSqlParserUtil.parse(sql);
        List<Column> columns = stmt.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (maskFieldNames.contains(columns.get(i).getColumnName())) {
                maskFieldIndexes.add(i);
            }
        }
        sql = replaceColumnValue(maskFieldIndexes, insertColumnValues, sql);
        System.out.println(sql);
    }

    @Test
    public void testParseUpdate() throws JSQLParserException {
        List<Integer> maskFieldIndexes = new ArrayList<>();
        String sql = "update SMS set SMS_HOSTNAME = ?, SMS_UPDATETIME = ?, SMS_SERVICESTATE = ?, SMS_STARTTIME = ?, SMS_PID = ?, SMS_CPU = ?, SMS_CPU_THRESHOLD = ?, SMS_RAM = ?, SMS_RAM_THRESHOLD = ?, SMS_THREADS = ?, SMS_THREADS_ACTIVE = ?, SMS_THREADS_THRESHOLD = ?, SMS_STOPTIME = ? where SMS_SERVICENAME = ? and SMS_SERVICEIP = ?";
        Update stmt = (Update) CCJSqlParserUtil.parse(sql);
        stmt.getWhere().accept(new WhereAdapter(maskFieldIndexes));
        List<UpdateSet> updateSets = stmt.getUpdateSets();
        for (UpdateSet updateSet : updateSets) {
            List<Column> columns = updateSet.getColumns();
            List<Expression> expressions = updateSet.getExpressions();
            if (CollectionUtils.isNotEmpty(columns) && CollectionUtils.isNotEmpty(expressions) && columns.size() == expressions.size()) {
                for (int i = 0; i < columns.size(); i++) {
                    Expression expression = expressions.get(i);
                    if (expression instanceof JdbcParameter && maskFieldNames.contains(columns.get(i).getColumnName())) {
                        maskFieldIndexes.add(((JdbcParameter) expression).getIndex() - 1);
                    }
                }
            } else {
                LogHelperFactory.getTraceLogger().warn("cannot handle UpdateSet = [", updateSet.toString(), "]");
            }
        }
        sql = replaceColumnValue(maskFieldIndexes, updateColumnValues, sql);
        System.out.println(sql);
    }

    private String replaceColumnValue(List<Integer> maskFieldIndexes, List<String> columnValues, String sql) {
        for (int i = 0; i < columnValues.size(); i++) {
            if (maskFieldIndexes.contains(i)) {
                sql = StringUtils.replace(sql, "?", "'MASK[" + StringUtils.repeat('*', columnValues.get(i).length()) + "]'", 1);
            } else {
                sql = StringUtils.replace(sql, "?", "'" + columnValues.get(i) + "'", 1);
            }
        }
        return sql;
    }

    private class WhereAdapter extends ExpressionVisitorAdapter {
        private final List<Integer> maskFieldIndexes;

        public WhereAdapter(List<Integer> maskFieldIndexes) {
            this.maskFieldIndexes = maskFieldIndexes;
        }

        @Override
        public void visit(EqualsTo expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(NotEqualsTo expr) {this.handleExpression(expr);}

        @Override
        public void visit(GreaterThan expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(GreaterThanEquals expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(MinorThan expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(MinorThanEquals expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(LikeExpression expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(InExpression expr) {
            Expression left = expr.getLeftExpression();
            ItemsList right = expr.getRightItemsList();
            if (left instanceof Column && right instanceof ExpressionList) {
                ExpressionList expressionList = (ExpressionList) right;
                List<Expression> expressions = expressionList.getExpressions();
                for (Expression expression : expressions) {
                    if (expression instanceof JdbcParameter) {
                        this.handleExpression((Column) left, (JdbcParameter) expression);
                    }
                }
            } else {
                LogHelperFactory.getTraceLogger().warn("cannot handle InExpression = [", expr.toString(), "]");
            }
        }

        private void handleExpression(BinaryExpression expr) {
            Expression left = expr.getLeftExpression();
            Expression right = expr.getRightExpression();
            if (left instanceof Column && right instanceof JdbcParameter) {
                this.handleExpression((Column) left, (JdbcParameter) right);
            } else if (left instanceof JdbcParameter && right instanceof Column) {
                this.handleExpression((Column) right, (JdbcParameter) left);
            } else {
                LogHelperFactory.getTraceLogger().warn("cannot handle BinaryExpression = [", expr.toString(), "]");
            }
        }

        private void handleExpression(Column column, JdbcParameter... jdbcParameters) {
            if (maskFieldNames.contains(column.getColumnName())) {
                for (JdbcParameter jdbcParameter : jdbcParameters) {
                    maskFieldIndexes.add(jdbcParameter.getIndex() - 1);
                }
            }
        }
    }
}
