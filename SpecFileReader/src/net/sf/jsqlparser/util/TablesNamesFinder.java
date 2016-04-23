/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.truncate.Truncate;

/**
 * Find all used tables within an select statement.
 */
public class TablesNamesFinder implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor, SelectItemVisitor, StatementVisitor  {

    private List<String> tables;
    /**
     * There are special names, that are not table names but are parsed as
     * tables. These names are collected here and are not included in the tables
     * - names anymore.
     */
    private List<String> otherItemNames;

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     *
     * @param delete
     * @return
     */
    public List<String> getTableList(Delete delete) {
        init();
        delete.accept(this);
        return tables;
    }

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     *
     * @param insert
     * @return
     */
    public List<String> getTableList(Insert insert) {
        init();
        insert.accept(this);
        return tables;
    }

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     *
     * @param replace
     * @return
     */
    public List<String> getTableList(Replace replace) {
        init();
        replace.accept(this);
        return tables;
    }

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     *
     * @param select
     * @return
     */
    public List<String> getTableList(Select select) {
        init();
        select.accept(this);
        return tables;
    }

    
    public void visit(Select select) {
        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                withItem.accept(this);
            }
        }
        select.getSelectBody().accept(this);
    }

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     *
     * @param update
     * @return
     */
    public List<String> getTableList(Update update) {
        init();
        update.accept(this);
        return tables;
    }

    public List<String> getTableList(CreateTable create) {
        init();
        create.accept(this);
        return tables;
    }
    
    public List<String> getTableList(Expression expr) {
        init();
        expr.accept(this);
        return tables;
    }

    
    public void visit(WithItem withItem) {
        otherItemNames.add(withItem.getName().toLowerCase());
        withItem.getSelectBody().accept(this);
    }

    
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem item : plainSelect.getSelectItems()) {
                item.accept(this);
            }
        }

        plainSelect.getFromItem().accept(this);

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                join.getRightItem().accept(this);
            }
        }
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }
        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(this);
        }
    }

    
    public void visit(Table tableName) {
        String tableWholeName = tableName.getFullyQualifiedName();
        if (!otherItemNames.contains(tableWholeName.toLowerCase())
                && !tables.contains(tableWholeName)) {
            tables.add(tableWholeName);
        }
    }

    
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(this);
    }

    
    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    
    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    
    public void visit(Column tableColumn) {
    }

    
    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    
    public void visit(DoubleValue doubleValue) {
    }

    
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);
    }

    
    public void visit(Function function) {
    }

    
    public void visit(GreaterThan greaterThan) {
        visitBinaryExpression(greaterThan);
    }

    
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryExpression(greaterThanEquals);
    }

    
    public void visit(InExpression inExpression) {
    	if(inExpression != null) {
    		inExpression.getLeftExpression().accept(this);
    	}
        if(inExpression != null) {
        	inExpression.getRightItemsList().accept(this);
        }
        
    }

    
    public void visit(SignedExpression signedExpression) {
        signedExpression.getExpression().accept(this);
    }

    
    public void visit(IsNullExpression isNullExpression) {
    }

    
    public void visit(JdbcParameter jdbcParameter) {
    }

    
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression);
    }

    
    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(this);
    }

    
    public void visit(LongValue longValue) {
    }

    
    public void visit(MinorThan minorThan) {
        visitBinaryExpression(minorThan);
    }

    
    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryExpression(minorThanEquals);
    }

    
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
    }

    
    public void visit(NullValue nullValue) {
    }

    
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    
    public void visit(StringValue stringValue) {
    }

    
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    
    public void visit(ExpressionList expressionList) {
        for (Expression expression : expressionList.getExpressions()) {
            expression.accept(this);
        }

    }

    
    public void visit(DateValue dateValue) {
    }

    
    public void visit(TimestampValue timestampValue) {
    }

    
    public void visit(TimeValue timeValue) {
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.CaseExpression)
     */
    
    public void visit(CaseExpression caseExpression) {
    	//caseExpression.getSwitchExpression().accept(this);
         for (Expression x : caseExpression.getWhenClauses()) {
             x.accept(this);
         }
         if(caseExpression.getElseExpression() != null) {
        	 caseExpression.getElseExpression().accept(this);
         }
         
         
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.WhenClause)
     */
    
    public void visit(WhenClause whenClause) {
    	whenClause.getWhenExpression().accept(this);
    }

    
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    
    public void visit(SubJoin subjoin) {
        subjoin.getLeft().accept(this);
        subjoin.getJoin().getRightItem().accept(this);
    }

    
    public void visit(Concat concat) {
        visitBinaryExpression(concat);
    }

    
    public void visit(Matches matches) {
        visitBinaryExpression(matches);
    }

    
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd);
    }

    
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr);
    }

    
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor);
    }

    
    public void visit(CastExpression cast) {
        cast.getLeftExpression().accept(this);
    }

    
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo);
    }

    
    public void visit(AnalyticExpression analytic) {
    }

    
    public void visit(SetOperationList list) {
        for (SelectBody plainSelect : list.getSelects()) {
            plainSelect.accept(this);
        }
    }

    
    public void visit(ExtractExpression eexpr) {
    }

    
    public void visit(LateralSubSelect lateralSubSelect) {
        lateralSubSelect.getSubSelect().getSelectBody().accept(this);
    }

    
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList exprList : multiExprList.getExprList()) {
            exprList.accept(this);
        }
    }

    
    public void visit(ValuesList valuesList) {
    }

    /**
     * Initializes table names collector.
     */
    protected void init() {
        otherItemNames = new ArrayList<String>();
        tables = new ArrayList<String>();
    }

    
    public void visit(IntervalExpression iexpr) {
    }

    
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
    }

    
    public void visit(OracleHierarchicalExpression oexpr) {
        if (oexpr.getStartExpression() != null) {
            oexpr.getStartExpression().accept(this);
        }

        if (oexpr.getConnectExpression() != null) {
            oexpr.getConnectExpression().accept(this);
        }
    }

    
    public void visit(RegExpMatchOperator rexpr) {
        visitBinaryExpression(rexpr);
    }

    
    public void visit(RegExpMySQLOperator rexpr) {
        visitBinaryExpression(rexpr);
    }

    
    public void visit(JsonExpression jsonExpr) {
    }

    
    public void visit(AllColumns allColumns) {
    }

    
    public void visit(AllTableColumns allTableColumns) {
    }

    
    public void visit(SelectExpressionItem item) {
        item.getExpression().accept(this);
    }

    
    public void visit(WithinGroupExpression wgexpr) {
    }

    
    public void visit(UserVariable var) {
    }

    
    public void visit(NumericBind bind) {

    }

    
    public void visit(KeepExpression aexpr) {
    }

    
    public void visit(MySQLGroupConcat groupConcat) {
    }

    
    public void visit(Delete delete) {
        tables.add(delete.getTable().getName());
        if (delete.getWhere() != null) {
            delete.getWhere().accept(this);
        }
    }

    
    public void visit(Update update) {
        for (Table table : update.getTables()) {
            tables.add(table.getName());
        }
        if (update.getExpressions() != null) {
            for (Expression expression : update.getExpressions()) {
                expression.accept(this);
            }
        }

        if (update.getFromItem() != null) {
            update.getFromItem().accept(this);
        }

        if (update.getJoins() != null) {
            for (Join join : update.getJoins()) {
                join.getRightItem().accept(this);
            }
        }

        if (update.getWhere() != null) {
            update.getWhere().accept(this);
        }
    }

    
    public void visit(Insert insert) {
        tables.add(insert.getTable().getName());
        if (insert.getItemsList() != null) {
            insert.getItemsList().accept(this);
        }
        if (insert.getSelect() != null) {
            visit(insert.getSelect());
        }
    }

    
    public void visit(Replace replace) {
        tables.add(replace.getTable().getName());
        if (replace.getExpressions() != null) {
            for (Expression expression : replace.getExpressions()) {
                expression.accept(this);
            }
        }
        if (replace.getItemsList() != null) {
            replace.getItemsList().accept(this);
        }
    }

    
    public void visit(Drop drop) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void visit(Truncate truncate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void visit(CreateIndex createIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void visit(CreateTable create) {
        tables.add(create.getTable().getFullyQualifiedName());
        if (create.getSelect() != null) {
            create.getSelect().accept(this);
        }
    }

    
    public void visit(CreateView createView) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void visit(Alter alter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void visit(Statements stmts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void visit(Execute execute) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void visit(SetStatement set) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void visit(RowConstructor rowConstructor) {
        for (Expression expr : rowConstructor.getExprList().getExpressions()) {
            expr.accept(this);
        }
    }

	
	public void visit(HexValue hexValue) {
		
	}

    
    public void visit(Merge merge) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public void visit(OracleHint hint) {
    }
    
}
