package com.starry.annotation.test;

import com.starry.annotation.Column;
import com.starry.annotation.Entity;
import com.starry.annotation.Index;
import com.starry.annotation.PrimaryKey;

@Entity(comment="状态数据表")
public class StatusData {
	@PrimaryKey
	@Column(type="varchar", length="20", nullAble=false, comment="编号")
	String id;
	
	@Index
	@Column(type="varchar", length="15", nullAble=false, comment="状态码")
	String statusCode;
	
	@Column(type="varchar", length="225", comment="状态描述")
	String statusDesc;
	
	@Index
	@Column(type="char", nullAble=false, comment="操作人编号")
	String operatorId;
	
	@Column(type="varchar", length="15", comment="操作人姓名")
	String operatorName;
	
	@Column(type="varchar", length="255", comment="备注")
	String mark;
}
