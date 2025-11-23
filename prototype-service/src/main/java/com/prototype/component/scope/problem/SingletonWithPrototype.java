package com.prototype.component.scope.problem;

import com.prototype.component.scope.PrototypeBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 싱글톤 빈 내에서 프로토타입 빈 사용시 문제가 발생
 * 싱글톤 빈은 생성 시점에 한 번만 의존성이 주입되므로, 프로토타입빈을 주입받아도
 * 항상 같은 인스턴스만 사용하게됨
 * */
@Component
public class SingletonWithPrototype {

    @Autowired
    private PrototypeBean prototypeBean;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ObjectProvider<PrototypeBean> prototypeBeansProvider;

    public void problematicUse() {
        System.out.println("문제가 있는 방식");
        prototypeBean.incrementCounter();
        prototypeBean.printInfo();
    }

    public void solutionWithContext() {
        System.out.println("해결책 1 - applicationContext 사용");
        PrototypeBean bean = context.getBean(PrototypeBean.class);
        bean.incrementCounter();
        bean.printInfo();
    }

    public void solutionWithProvider() {
        System.out.println("해결책 2 - ObjectProvider 사용");
        PrototypeBean bean = prototypeBeansProvider.getObject();
        bean.incrementCounter();
        bean.printInfo();
    }

}
