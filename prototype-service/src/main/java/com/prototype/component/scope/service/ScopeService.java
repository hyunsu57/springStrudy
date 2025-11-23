package com.prototype.component.scope.service;


public class ScopeService {
    private final String name;

    public ScopeService(String name) {
        this.name= name;
        System.out.println(name + " 생성 - 해시코드: " + this.hashCode());
    }

    public String getName() {
        return name;
    }
}
