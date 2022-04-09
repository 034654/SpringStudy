package com.mrlv.demo.mvc.action;

import com.mrlv.demo.service.IDemoService;
import com.mrlv.mvcframework.annotation.MrAutowired;
import com.mrlv.mvcframework.annotation.MrController;
import com.mrlv.mvcframework.annotation.MrRequestMapping;
import com.mrlv.mvcframework.annotation.MrRequestParam;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@MrController
@MrRequestMapping("/demo")
public class DemoAction {

  	@MrAutowired private IDemoService demoService;

	@MrRequestMapping("/query.*")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @MrRequestParam("name") String name){

		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MrRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@MrRequestParam("a") Integer a, @MrRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MrRequestMapping("/sub")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@MrRequestParam("a") Double a, @MrRequestParam("b") Double b){
		try {
			resp.getWriter().write(a + "-" + b + "=" + (a - b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MrRequestMapping("/remove")
	public String  remove(@MrRequestParam("id") Integer id){
		return "" + id;
	}

}
