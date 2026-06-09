package hu.backend.observer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ObserverInitializerTest {

    @Mock
    TaskStatusSubject taskStatusSubject;

    @Mock
    LoggingTaskObserver loggingTaskObserver;

    @Mock
    AuditTaskObserver auditTaskObserver;

    @InjectMocks
    ObserverInitializer observerInitializer;

    @Test
    void shouldRegisterObserversOnInitialization() {
        observerInitializer.init();

        verify(taskStatusSubject).addObserver(loggingTaskObserver);
        verify(taskStatusSubject).addObserver(auditTaskObserver);
    }

    @Test
    void shouldRegisterLoggingObserverBeforeAuditObserver() {
        observerInitializer.init();

        InOrder inOrder = inOrder(taskStatusSubject);

        inOrder.verify(taskStatusSubject).addObserver(loggingTaskObserver);
        inOrder.verify(taskStatusSubject).addObserver(auditTaskObserver);
    }
}