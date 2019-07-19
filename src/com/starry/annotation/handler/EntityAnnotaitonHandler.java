package com.starry.annotation.handler;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.starry.annotation.Column;
import com.starry.annotation.Entity;
import com.starry.annotation.Index;
import com.starry.annotation.PrimaryKey;

public class EntityAnnotaitonHandler {

	private static final String STRING_EMPTY = "";
	private static final String STRING_BLANK = " ";
	private static final String CONNECTOR = "_";
	private static final String NAME_DOT = ".";
	private static final String COMMA = ",";
	private static final String COLON = ";";
	
	private static final String LEFT_BRACKETS = "(";
	private static final String RIGHT_BRACKETS = ")";
	private static final String SINGLE_QUOTATION = "'";
	
	private static final String AS = "AS";
	private static final String NOT_NULL = "NOT NULL";
	private static final String COMMENT = "COMMENT";
	private static final String INDEX = "INDEX";
	
	public <T> List<String> process(T entity) throws RuntimeException {
		if (entity == null) {
			return null;
		}
		Class<? extends Object> entityClass = entity.getClass();
		if (!entityClass.isAnnotationPresent(Entity.class)) {
			return null;
		}
		Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
		String[] entityOperator = entityAnnotation.operator();
		
		List<String> statementList = new LinkedList<String>();
		for (String operatorStr : entityOperator) {
			if ("all".equals(operatorStr)) {
				statementList.add(getCreateStatement(entityClass));
				statementList.add(getDropStatement(entityClass));
				statementList.add(getSelectStatement(entityClass));
			} else if ("create".equals(operatorStr)) {
				statementList.add(getCreateStatement(entityClass));
			} else if ("drop".equals(operatorStr)) {
				statementList.add(getDropStatement(entityClass));
			} else if ("select".equals(operatorStr)) {
				statementList.add(getSelectStatement(entityClass));
			} else {
				throw new RuntimeException("value of operator can not matcher.");
			}
		}
		
		return statementList;
		
	}
	
	/**
	 * 获取创建语句
	 * @param entityClass
	 * @return
	 */
	public String getCreateStatement(Class<? extends Object> entityClass) {
		
		Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
		// 表信息
		String entityComment = entityAnnotation.comment();
		String entityName = getEntityName(entityClass.getName());   // 表名
		// 字段
		String primaryKey = null;
		List<String> indexList = new LinkedList<String>();
		List<String[]> statementList = new LinkedList<String[]>();
		Field[] fields = entityClass.getDeclaredFields();
		for (Field field : fields) {
			
			if (!field.isAnnotationPresent(Column.class)) {
				continue;
			}
			String fieldName = getEntityName(field.getName());
			if (field.isAnnotationPresent(PrimaryKey.class)) {
				primaryKey = fieldName;
			}
			
			if (field.isAnnotationPresent(Index.class)) {
				indexList.add(fieldName);
			}
			
			String[] columnStr = new String[4];
			Column columnField = field.getAnnotation(Column.class);
			columnStr[0] = fieldName;
			
			StringBuilder fieldType = new StringBuilder();
			fieldType.append(columnField.type().toUpperCase());
			fieldType.append(LEFT_BRACKETS);
			fieldType.append(columnField.length());
			fieldType.append(RIGHT_BRACKETS);
			columnStr[1] = fieldType.toString();
			
			if (!columnField.nullAble()){
				columnStr[2] = NOT_NULL;
			} else {
				columnStr[2] = STRING_EMPTY;
			}
			
			if (STRING_EMPTY.equals(columnField.comment())) {
				columnStr[3] = STRING_EMPTY;
			} else {
				StringBuilder fieldComment = new StringBuilder();
				fieldComment.append(COMMENT);
				fieldComment.append(STRING_BLANK);
				fieldComment.append(SINGLE_QUOTATION);
				fieldComment.append(columnField.comment());
				fieldComment.append(SINGLE_QUOTATION);
				columnStr[3] = fieldComment.toString();
			}
			
			statementList.add(columnStr);
		}
		
		// 主键
		if (primaryKey != null) {
			String[] primaryKeyStr = new String[2];
			primaryKeyStr[0] = "PRIMARY KEY";
			primaryKeyStr[1] = LEFT_BRACKETS + primaryKey + RIGHT_BRACKETS;
			statementList.add(primaryKeyStr);
 		}
		
		// 索引
		if (indexList.size() > 0) {
			for (String indexTemp : indexList) {
				String[] indexStr = new String[3];
				indexStr[0] = INDEX;
				indexStr[1] = INDEX + CONNECTOR + indexTemp;
				indexStr[2] = LEFT_BRACKETS + indexTemp + RIGHT_BRACKETS;
				statementList.add(indexStr);
			}
		} 
		
		// 拼接
		StringBuilder createTableStatement = new StringBuilder();
		createTableStatement.append("CREATE TABLE");
		createTableStatement.append(STRING_BLANK);
		createTableStatement.append(entityName);
		createTableStatement.append(LEFT_BRACKETS);
		for (String[] columnStr : statementList) {
			for (String string : columnStr) {
				if (STRING_EMPTY.equals(string)) {
					continue;
				}
				createTableStatement.append(string);
				createTableStatement.append(STRING_BLANK);
			}
			createTableStatement.deleteCharAt(createTableStatement.lastIndexOf(STRING_BLANK));
			createTableStatement.append(COMMA);
		}
		
 		createTableStatement.deleteCharAt(createTableStatement.lastIndexOf(COMMA));

		createTableStatement.append(RIGHT_BRACKETS);
		createTableStatement.append("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=");
		createTableStatement.append(SINGLE_QUOTATION);
		createTableStatement.append(entityComment);
		createTableStatement.append(SINGLE_QUOTATION);
		createTableStatement.append(COLON);
		
		return createTableStatement.toString();
	}
	
	/**
	 * 获取drop语句
	 * @param entityClass
	 * @return
	 */
	public String getDropStatement(Class<? extends Object> entityClass) {
		String entityName = getEntityName(entityClass.getName());   // 表名
		StringBuilder dropStatement = new StringBuilder();
		dropStatement.append("DROP TABLE IS EXISTS ");
		dropStatement.append(entityName);
		return dropStatement.toString();
	}
	
	/**
	 * 获取select语句
	 * @param entityClass
	 * @return
	 */
	public String getSelectStatement(Class<? extends Object> entityClass) {
		String entityName = getEntityName(entityClass.getName());   // 表名
		Field[] fields = entityClass.getDeclaredFields();
		
		List<String[]> fieldList = new LinkedList<String[]>();
		for (Field field : fields) {
			if (!field.isAnnotationPresent(Column.class)) {
				continue;
			}
			String[] tempStatement = new String[3];
			String fieldName = field.getName();
			tempStatement[0] = getEntityName(fieldName);
			tempStatement[1] = AS;
			tempStatement[2] = fieldName;
			fieldList.add(tempStatement);
		}
		
		StringBuilder selectStatement = new StringBuilder();
		selectStatement.append("SELECT");
		selectStatement.append(STRING_BLANK);
		for (String[] stringTemp : fieldList) {
			selectStatement.append(stringTemp[0]);
			selectStatement.append(STRING_BLANK);
			selectStatement.append(stringTemp[1]);
			selectStatement.append(STRING_BLANK);
			selectStatement.append(stringTemp[2]);
			selectStatement.append(COMMA);
		}
		selectStatement.deleteCharAt(selectStatement.lastIndexOf(COMMA));
		selectStatement.append(STRING_BLANK);
		selectStatement.append("FROM");
		selectStatement.append(STRING_BLANK);
		selectStatement.append(entityName);
		
		return selectStatement.toString();
	}
	
	/**
	 * 根据类名获取表名
	 * @param entityName  (全路径)类名
	 * @return 符合mysql规范的表名
	 */
	public String getEntityName(String entityName) {
		if (entityName == null || STRING_EMPTY.equals(entityName)) {
			throw new RuntimeException("entityName is null or empty.");
		}
		if (entityName.contains(NAME_DOT)) {
			entityName = entityName.substring(entityName.lastIndexOf(NAME_DOT) + 1);
		}
		
		List<String> nameList = new LinkedList<String>();
		Stack<Character> stack = new Stack<Character>();
		for (int index = entityName.length() - 1; index >= 0; index--) {
			char indexChar = entityName.charAt(index);
			if (indexChar >= 65 && indexChar <= 90) {
				stack.add(indexChar);
				StringBuilder sBuilder = new StringBuilder();
				while (!stack.isEmpty()) {
					sBuilder.append(stack.pop());
				}
				nameList.add(sBuilder.toString().toUpperCase());
			} else {
				stack.add(indexChar);
			}
		}
		
		// 处理field
		if (!stack.isEmpty()) {
			StringBuilder sBuilder = new StringBuilder();
			while (!stack.isEmpty()) {
				sBuilder.append(stack.pop());
			}
			nameList.add(sBuilder.toString().toUpperCase());
		}
		
		StringBuilder entityNameStr = new StringBuilder();
		for (int index = nameList.size() - 1; index >= 0; index-- ) {
			entityNameStr.append(nameList.get(index));
			entityNameStr.append(CONNECTOR);
		}
		
		return entityNameStr.substring(0, entityNameStr.length()-1);
	}
}
