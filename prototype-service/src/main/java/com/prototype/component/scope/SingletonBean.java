package com.prototype.component.scope;

import org.springframework.stereotype.Component;

@Component
public class SingletonBean {
	private int counter = 0;

	public SingletonBean() {
		System.out.println("singleton bean 생성자 호출 : "+ this.hashCode());
	}

	public void incrementCounter() {
		counter++;
	}

	public void printInfo() {
		System.out.println("singleton bean - 해시코드 : " + this.hashCode() + " , counter : "+counter);
	}
}
