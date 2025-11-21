package com.prototype;

import com.prototype.component.scope.PrototypeBean;
import com.prototype.component.scope.SingletonBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ScopeExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScopeExampleApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(ApplicationContext context) {
		return args -> {
			SingletonBean singleton1 = context.getBean(SingletonBean.class);
			SingletonBean singleton2 = context.getBean(SingletonBean.class);
			SingletonBean singleton3 = context.getBean(SingletonBean.class);

			singleton1.incrementCounter();
			singleton2.incrementCounter();
			singleton3.incrementCounter();

			singleton1.printInfo();
			singleton2.printInfo();
			singleton3.printInfo();

			System.out.println("동일한 객체인가? " + (singleton1 == singleton2 && singleton2 == singleton3));

			System.out.println("\n========== 프로토타입 스코프 테스트 ==========");
			PrototypeBean prototype1 = context.getBean(PrototypeBean.class);
			PrototypeBean prototype2 = context.getBean(PrototypeBean.class);
			PrototypeBean prototype3 = context.getBean(PrototypeBean.class);

			prototype1.incrementCounter();
			prototype2.incrementCounter();
			prototype3.incrementCounter();

			prototype1.printInfo();
			prototype2.printInfo();
			prototype3.printInfo();

			System.out.println("동일한 객체인가? " + (prototype1 == prototype2 && prototype2 == prototype3));
		};
	}
}
