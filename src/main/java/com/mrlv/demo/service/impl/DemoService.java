package com.mrlv.demo.service.impl;

/**
 * 核心业务逻辑
 */
import com.mrlv.demo.service.IDemoService;
import com.mrlv.mvcframework.annotation.MrService;

@MrService
public class DemoService implements IDemoService {

	@Override
	public String get(String name) {
		return "My name is " + name;
	}

}
