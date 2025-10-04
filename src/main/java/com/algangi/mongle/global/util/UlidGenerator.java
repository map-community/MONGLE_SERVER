package com.algangi.mongle.global.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import com.github.f4b6a3.ulid.UlidCreator;

import java.io.Serializable;

public class UlidGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        return UlidCreator.getUlid().toString();
    }
}
