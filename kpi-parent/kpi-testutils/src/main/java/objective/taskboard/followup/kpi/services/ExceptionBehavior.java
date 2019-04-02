package objective.taskboard.followup.kpi.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.AbstractThrowableAssert;

public class ExceptionBehavior implements DSLSimpleBehaviorWithAsserter<ExceptionBehavior.ThrowableAsserter>{

    private DSLSimpleBehavior delegateBehavior;
    private AbstractThrowableAssert<?, ? extends Throwable> exception;

    public ExceptionBehavior(DSLSimpleBehavior defaulBehavior) {
        this.delegateBehavior = defaulBehavior;
    }

    @Override
    public void behave(KpiEnvironment environment) {
        exception = assertThatThrownBy(() -> delegateBehavior.behave(environment));
    };
    @Override
    public ThrowableAsserter then() {
        return new ThrowableAsserter(exception);
    }

    public static class ThrowableAsserter {

        private AbstractThrowableAssert<?, ? extends Throwable> subject;

        public ThrowableAsserter(AbstractThrowableAssert<?, ? extends Throwable> subject) {
            this.subject = subject;
        }

        public ThrowableAsserter isFromException(Class<?> _class) {
            this.subject.isInstanceOf(_class);
            return this;
        }

        public ThrowableAsserter hasMessage(String message) {
           this.subject.hasMessage(message);
            return this;
        }


    }


}
