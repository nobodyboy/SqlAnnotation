package com.starry.annotation.test;

import java.util.List;

import com.starry.annotation.handler.EntityAnnotaitonHandler;

public class Client {
	public static void main(String[] args) {
		EntityAnnotaitonHandler handler = new EntityAnnotaitonHandler();
		List<String> list= null;
		try {
			list = handler.process(new StatusData());
		} catch (Exception e) {
			System.out.println("发生错误: " + e.getMessage());
		}
		
		if (list == null) {
			return ;
		}
		
		for (String string : list) {
			System.out.println(string);
			System.out.println();
		}
	}
}
