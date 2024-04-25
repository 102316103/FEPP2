package com.syscom.fep.mybatis.interceptor;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.BaseJdbcLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.ArrayUtil;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.fep.mybatis.interceptor.logger")
@ConditionalOnProperty(prefix = "spring.fep.mybatis.interceptor.logger", name = "enable", havingValue = "true")
@RefreshScope
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
})
public class LoggerInterceptor implements Interceptor {
    private final LogHelper trace = LogHelperFactory.getTraceLogger();
    private final LogHelper jdbc = LogHelperFactory.getJdbcLogger();
    /**
     * SQL中哪些欄位名字的值需要進行遮蔽處理
     */
    @Value("#{'${spring.fep.mybatis.interceptor.logger.maskFieldNames:}'.split(',')}")
    private List<String> maskFieldNames;
    private String maskString;
    private String maskCharacter = "*";
    private int maskCharacterLength = 8;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        if (statementHandler != null) {
            BoundSql boundSql = statementHandler.getBoundSql();
            if (boundSql != null) {
                String sql = SqlSourceBuilder.removeExtraWhitespaces(boundSql.getSql());
                Object args0 = invocation.getArgs()[0];
                sql = this.parseSQL(sql, args0);
                try {
                    BaseStatementHandler baseStatementHandler = ReflectUtil.getFieldValue(statementHandler, "delegate", null);
                    MappedStatement mappedStatement = ReflectUtil.getFieldValue(baseStatementHandler, "mappedStatement", null);
                    jdbc.debug("[", mappedStatement.getId(), "] ==> ", sql);
                } catch (Throwable t) {
                    jdbc.debug("[Execute SQL] ==> ", sql);
                }
            }
        }
        return invocation.proceed();
    }

    /**
     * 解讀SQL, 並針對欄位進行遮蔽處理
     *
     * @param sql
     * @param args0
     * @return
     */
    private String parseSQL(String sql, Object args0) {
        List<Integer> maskFieldIndexes = new ArrayList<>();
        RefBoolean modifiedSQL = new RefBoolean(false);
        if (CollectionUtils.isNotEmpty(maskFieldNames)) {
            try {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sql);
                if (statement instanceof Select) {
                    Select select = (Select) statement;
                    PlainSelect selectBody = (PlainSelect) select.getSelectBody();
                    Expression where = selectBody.getWhere();
                    if (where != null) {
                        where.accept(new WhereAdapter(maskFieldIndexes, modifiedSQL));
                    }
                } else if (statement instanceof Insert) {
                    Insert insert = (Insert) statement;
                    List<Column> columns = insert.getColumns();
                    if (CollectionUtils.isNotEmpty(columns)) {
                        for (int i = 0; i < columns.size(); i++) {
                            if (maskFieldNames.contains(columns.get(i).getColumnName())) {
                                maskFieldIndexes.add(i);
                            }
                        }
                    }
                } else if (statement instanceof Update) {
                    Update update = (Update) statement;
                    Expression where = update.getWhere();
                    if (where != null) {
                        where.accept(new WhereAdapter(maskFieldIndexes, modifiedSQL));
                    }
                    List<UpdateSet> updateSets = update.getUpdateSets();
                    if (CollectionUtils.isNotEmpty(updateSets)) {
                        for (UpdateSet updateSet : updateSets) {
                            List<Column> columns = updateSet.getColumns();
                            List<Expression> expressions = updateSet.getExpressions();
                            if (CollectionUtils.isNotEmpty(columns) && CollectionUtils.isNotEmpty(expressions) && columns.size() == expressions.size()) {
                                for (int i = 0; i < columns.size(); i++) {
                                    Column column = columns.get(i);
                                    Expression expression = expressions.get(i);
                                    this.handleSQLExpression(column, expression, maskFieldIndexes, modifiedSQL);
                                }
                            } else {
                                trace.warn("cannot handle UpdateSet = [", updateSet.toString(), "]");
                            }
                        }
                    }
                } else if (statement instanceof Delete) {
                    Delete delete = (Delete) statement;
                    Expression where = delete.getWhere();
                    if (where != null) {
                        where.accept(new WhereAdapter(maskFieldIndexes, modifiedSQL));
                    }
                } else {
                    trace.warn("Ignore Statement:", statement.getClass().getSimpleName(), ", sql=[", sql, "]");
                }
                if (modifiedSQL.get())
                    sql = statement.toString();
            } catch (JSQLParserException e) {
                trace.warn(e, "Parse SQL Failed, sql = [", sql, "]", e.getMessage());
            }
        }
        sql = replaceColumnValue(maskFieldIndexes, args0, sql);
        return sql;
    }

    private String replaceColumnValue(List<Integer> maskFieldIndexes, Object args0, String sql) {
        if (args0 == null)
            return sql;
        try {
            List<Object> columnValues = null;
            if (Proxy.isProxyClass(args0.getClass())) {
                BaseJdbcLogger preparedStatementLogger = (BaseJdbcLogger) Proxy.getInvocationHandler(args0);
                columnValues = ReflectUtil.getFieldValue(preparedStatementLogger, "columnValues", null);
            } else {
                trace.warn(args0.getClass(), " was not Proxy Class!!!");
            }
            if (CollectionUtils.isNotEmpty(columnValues)) {
                for (int i = 0; i < columnValues.size(); i++) {
                    Object columnValue = columnValues.get(i);
                    if (CollectionUtils.isNotEmpty(maskFieldIndexes) && maskFieldIndexes.contains(i)) {
                        sql = StringUtils.replace(sql, "?", this.getMaskString(true), 1);
                    } else {
                        sql = StringUtils.replace(sql, "?", this.getColumnValue(columnValue), 1);
                    }
                }
            }
        } catch (Throwable t) {
            trace.warn(t, t.getMessage());
        }
        return sql;
    }

    private String getColumnValue(Object columnValue) {
        if (columnValue == null) {
            return "null";
        }
        String value = null;
        if (columnValue instanceof Array) {
            try {
                value = ArrayUtil.toString(((Array) columnValue).getArray());
            } catch (SQLException e) {
                trace.warn(e, e.getMessage());
            }
        } else if (columnValue instanceof StringReader) {
            StringReader reader = (StringReader) columnValue;
            try {
                reader.reset();
            } catch (IOException e) {
                trace.warn(e, e.getMessage());
            }
            try {
                value = IOUtils.toString(reader);
            } catch (IOException e) {
                trace.warn(e, e.getMessage());
            }
        }
        if (value == null) {
            value = columnValue.toString();
        }
        if (columnValue instanceof String || columnValue instanceof Character || columnValue instanceof StringBuilder || columnValue instanceof Timestamp || columnValue instanceof StringReader) {
            return StringUtils.join("'", value, "'");
        }
        return value;
    }

    public List<String> getMaskFieldNames() {
        return maskFieldNames;
    }

    public void setMaskFieldNames(List<String> maskFieldNames) {
        this.maskFieldNames = maskFieldNames;
    }

    public String getMaskString(boolean quote) {
        if (StringUtils.isBlank(maskString)) {
            maskString = StringUtils.join("MASK[", StringUtils.repeat(maskCharacter, maskCharacterLength), "]");
            if (quote)
                maskString = StringUtils.join("'", maskString, "'");
        }
        return maskString;
    }

    public void setMaskString(String maskString) {
        this.maskString = maskString;
    }

    public String getMaskCharacter() {
        return maskCharacter;
    }

    public void setMaskCharacter(String maskCharacter) {
        this.maskCharacter = maskCharacter;
    }

    public int getMaskCharacterLength() {
        return maskCharacterLength;
    }

    public void setMaskCharacterLength(int maskCharacterLength) {
        this.maskCharacterLength = maskCharacterLength;
    }

    private class WhereAdapter extends ExpressionVisitorAdapter {
        private final List<Integer> maskFieldIndexes;
        private final RefBoolean modifiedSQL;

        public WhereAdapter(List<Integer> maskFieldIndexes, RefBoolean modifiedSQL) {
            this.maskFieldIndexes = maskFieldIndexes;
            this.modifiedSQL = modifiedSQL;
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
                handleSQLExpression((Column) left, expressions, maskFieldIndexes, modifiedSQL);
            } else {
                trace.warn("cannot handle InExpression = [", expr.toString(), "]");
            }
        }

        private void handleExpression(BinaryExpression expr) {
            Expression left = expr.getLeftExpression();
            Expression right = expr.getRightExpression();
            if (left instanceof Column) {
                handleSQLExpression((Column) left, right, maskFieldIndexes, modifiedSQL);
            } else if (right instanceof Column) {
                handleSQLExpression((Column) right, left, maskFieldIndexes, modifiedSQL);
            } else {
                trace.warn("cannot handle BinaryExpression = [", expr.toString(), "]");
            }
        }
    }

    private void handleSQLExpression(Column column, Expression expression, List<Integer> maskFieldIndexes, RefBoolean modifiedSQL) {
        this.handleSQLExpression(column, Collections.singletonList(expression), maskFieldIndexes, modifiedSQL);
    }

    private void handleSQLExpression(Column column, List<Expression> expressions, List<Integer> maskFieldIndexes, RefBoolean modifiedSQL) {
        if (maskFieldNames.contains(column.getColumnName())) {
            for (Expression expression : expressions) {
                if (expression instanceof JdbcParameter) {
                    maskFieldIndexes.add(((JdbcParameter) expression).getIndex() - 1);
                } else if (expression instanceof StringValue) {
                    ((StringValue) expression).setValue(getMaskString(false));
                    modifiedSQL.set(true);
                } else {
                    trace.warn("Cannot handle [", column.getColumnName(), "] with value [", StringUtils.join(expressions, ","), "]");
                }
            }
        }
    }
}
