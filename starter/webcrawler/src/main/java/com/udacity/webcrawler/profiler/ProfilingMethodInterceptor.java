package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final ProfilingState profilingState;
  private final Object delegate;

  // You will need to add more instance fields and constructor arguments to this class.
  ProfilingMethodInterceptor(Clock clock, ProfilingState profilingState, Object delegate) {
    this.clock = Objects.requireNonNull(clock);
    this.profilingState = Objects.requireNonNull(profilingState);
    this.delegate = Objects.requireNonNull(delegate);
  }

  /**
   * A method interceptor that checks whether {@link Method}s are annotated with the {@link
   * Profiled} annotation. If they are, the method interceptor records how long the method
   * invocation took.
   *
   * @param proxy  the proxy object.
   * @param method the method being called.
   * @param args   the arguments to the method call.
   * @return the result of the method call.
   * @throws Throwable if the method call throws an exception.
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.
    boolean profiled = method.getAnnotation(Profiled.class) != null;
    Instant start = profiled ? clock.instant() : null;
    Object result = null;
 
    try {
      result = method.invoke(delegate, args);
    } catch (InvocationTargetException ex) {
      throw ex.getTargetException();
    } catch(IllegalAccessException ex){
      throw new RuntimeException(ex);
    } finally {
      if (profiled) {
          Duration duration = Duration.between(start, clock.instant());
          profilingState.record(delegate.getClass(), method, duration);
      }
    }
    return result;
  }
}
