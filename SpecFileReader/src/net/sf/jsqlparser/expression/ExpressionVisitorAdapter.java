/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2015 JSQLParser
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
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.ExpressionListItem;
import net.sf.jsqlparser.statement.select.FunctionItem;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

public class ExpressionVisitorAdapter implements ExpressionVisitor, ItemsListVisitor, PivotVisitor, SelectItemVisitor {

    private SelectVisitor selectVisitor;

    public SelectVisitor getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor selectVisitor) {
        this.selectVisitor = selectVisitor;
    }

    
    public void visit(NullValue value) {

    }

    
    public void visit(Function function) {
        if (function.getParameters() != null) {
            function.getParameters().accept(this);
        }
        if (function.getKeep() != null) {
            function.getKeep().accept(this);
        }
    }

    
    public void visit(SignedExpression expr) {
        expr.getExpression().accept(this);
    }

    
    public void visit(JdbcParameter parameter) {

    }

    
    public void visit(JdbcNamedParameter parameter) {

    }

    
    public void visit(DoubleValue value) {

    }

    
    public void visit(LongValue value) {

    }

    
    public void visit(DateValue value) {

    }

    
    public void visit(TimeValue value) {

    }

    
    public void visit(TimestampValue value) {

    }

    
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    
    public void visit(StringValue value) {

    }

    
    public void visit(Addition expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(Division expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(Multiplication expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(Subtraction expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(AndExpression expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(OrExpression expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(Between expr) {
        expr.getLeftExpression().accept(this);
        expr.getBetweenExpressionStart().accept(this);
        expr.getBetweenExpressionEnd().accept(this);
    }

    
    public void visit(EqualsTo expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(GreaterThan expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(GreaterThanEquals expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(InExpression expr) {
        if (expr.getLeftExpression() != null) {
            expr.getLeftExpression().accept(this);
        } else if (expr.getLeftItemsList() != null) {
            expr.getLeftItemsList().accept(this);
        }
        expr.getRightItemsList().accept(this);
    }

    
    public void visit(IsNullExpression expr) {
        expr.getLeftExpression().accept(this);
    }

    
    public void visit(LikeExpression expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(MinorThan expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(MinorThanEquals expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(NotEqualsTo expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(Column column) {

    }

    
    public void visit(SubSelect subSelect) {
        if (selectVisitor != null) {
            for (WithItem item : subSelect.getWithItemsList()) {
                item.accept(selectVisitor);
            }
            subSelect.getSelectBody().accept(selectVisitor);
        }
        if (subSelect.getPivot() != null) {
            subSelect.getPivot().accept(this);
        }
    }

    
    public void visit(CaseExpression expr) {
        expr.getSwitchExpression().accept(this);
        for (Expression x : expr.getWhenClauses()) {
            x.accept(this);
        }
        expr.getElseExpression().accept(this);
    }

    
    public void visit(WhenClause expr) {
        expr.getWhenExpression().accept(this);
        expr.getThenExpression().accept(this);
    }

    
    public void visit(ExistsExpression expr) {
        expr.getRightExpression().accept(this);
    }

    
    public void visit(AllComparisonExpression expr) {

    }

    
    public void visit(AnyComparisonExpression expr) {

    }

    
    public void visit(Concat expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(Matches expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(BitwiseAnd expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(BitwiseOr expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(BitwiseXor expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(CastExpression expr) {
        expr.getLeftExpression().accept(this);
    }

    
    public void visit(Modulo expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(AnalyticExpression expr) {
        expr.getExpression().accept(this);
        expr.getDefaultValue().accept(this);
        expr.getOffset().accept(this);
        if (expr.getKeep() != null) {
            expr.getKeep().accept(this);
        }
        for (OrderByElement element : expr.getOrderByElements()) {
            element.getExpression().accept(this);
        }

        expr.getWindowElement().getRange().getStart().getExpression().accept(this);
        expr.getWindowElement().getRange().getEnd().getExpression().accept(this);
        expr.getWindowElement().getOffset().getExpression().accept(this);
    }

    
    public void visit(ExtractExpression expr) {
        expr.getExpression().accept(this);
    }

    
    public void visit(IntervalExpression expr) {
    }

    
    public void visit(OracleHierarchicalExpression expr) {
        expr.getConnectExpression().accept(this);
        expr.getStartExpression().accept(this);
    }

    
    public void visit(RegExpMatchOperator expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(ExpressionList expressionList) {
        for (Expression expr : expressionList.getExpressions()) {
            expr.accept(this);
        }
    }

    
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList list : multiExprList.getExprList()) {
            visit(list);
        }
    }

    protected void visitBinaryExpression(BinaryExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
    }

    
    public void visit(JsonExpression jsonExpr) {
        visit(jsonExpr.getColumn());
    }

    
    public void visit(RegExpMySQLOperator expr) {
        visitBinaryExpression(expr);
    }

    
    public void visit(WithinGroupExpression wgexpr) {
        wgexpr.getExprList().accept(this);
        for (OrderByElement element : wgexpr.getOrderByElements()) {
            element.getExpression().accept(this);
        }
    }

    
    public void visit(UserVariable var) {

    }

    
    public void visit(NumericBind bind) {

    }

    
    public void visit(KeepExpression expr) {
        for (OrderByElement element : expr.getOrderByElements()) {
            element.getExpression().accept(this);
        }
    }

    
    public void visit(MySQLGroupConcat groupConcat) {
        for (Expression expr : groupConcat.getExpressionList().getExpressions()) {
            expr.accept(this);
        }
        if (groupConcat.getOrderByElements() != null) {
            for (OrderByElement element : groupConcat.getOrderByElements()) {
                element.getExpression().accept(this);
            }
        }
    }

    
    public void visit(Pivot pivot) {
        for (FunctionItem item : pivot.getFunctionItems()) {
            item.getFunction().accept(this);
        }
        for (Column col : pivot.getForColumns()) {
            col.accept(this);
        }
        if (pivot.getSingleInItems() != null) {
            for (SelectExpressionItem item : pivot.getSingleInItems()) {
                item.accept(this);
            }
        }

        if (pivot.getMultiInItems() != null) {
            for (ExpressionListItem item : pivot.getMultiInItems()) {
                item.getExpressionList().accept(this);
            }
        }
    }

    
    public void visit(PivotXml pivot) {
        for (FunctionItem item : pivot.getFunctionItems()) {
            item.getFunction().accept(this);
        }
        for (Column col : pivot.getForColumns()) {
            col.accept(this);
        }
        if (pivot.getInSelect() != null) {
            if (selectVisitor != null) {
                pivot.getInSelect().accept(selectVisitor);
            }
        }
    }

    
    public void visit(AllColumns allColumns) {

    }

    
    public void visit(AllTableColumns allTableColumns) {

    }

    
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
    }

    
    public void visit(RowConstructor rowConstructor) {
        for (Expression expr : rowConstructor.getExprList().getExpressions()) {
            expr.accept(this);
        }
    }

	
	public void visit(HexValue hexValue) {
		
	}
        
    
    public void visit(OracleHint hint) {

    }
        
}
