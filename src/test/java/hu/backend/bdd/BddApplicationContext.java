package hu.backend.bdd;

import io.micronaut.context.ApplicationContext;

public final class BddApplicationContext {

    private static final ApplicationContext CONTEXT = ApplicationContext.run();

    private BddApplicationContext() {
    }

    public static <T> T getBean(Class<T> beanType) {
        return CONTEXT.getBean(beanType);
    }

    public static void close() {
        CONTEXT.close();
    }
}