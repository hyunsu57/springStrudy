package com.prototype.component.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype") // 스코프를 명시해줘야한다, 아니면 기본이 singleton으로 생성된다.
public class PrototypeBean {

	private int counter = 0;

	public PrototypeBean() {
		System.out.println("prototype bean 생성자 호출 : " + this.hashCode());
	}

	public void incrementCounter() {
		counter++;
	}

	public int getCounter(){
		return counter++;
	}

	public void printInfo() {
		System.out.println("prototype bean - 해시코드 : "+ this.hashCode() + ", counter : "+ counter);
	}

}
