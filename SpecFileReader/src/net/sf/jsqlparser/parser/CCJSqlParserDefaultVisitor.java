/* Generated By:JavaCC: Do not edit this line. CCJSqlParserDefaultVisitor.java Version 6.1_2 */
package net.sf.jsqlparser.parser;

public class CCJSqlParserDefaultVisitor implements CCJSqlParserVisitor{
  public Object defaultVisit(SimpleNode node, Object data){
    node.childrenAccept(this, data);
    return data;
  }
  public Object visit(SimpleNode node, Object data){
    return defaultVisit(node, data);
  }
}
/* JavaCC - OriginalChecksum=dda620678a6218e91238e756ecc1ce7a (do not edit this line) */
