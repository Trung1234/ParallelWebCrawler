package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.*;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  /**
   * Returns the {@link ProfilingState} object that this class uses to track profiling data.
   */
  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
 
    // Use a dynamic proxy (java.lang.reflect.Proxy) to "wrap" the delegate in a
    //       ProfilingMethodInterceptor and return a dynamic proxy from this method.
    //       See https://docs.oracle.com/javase/10/docs/api/java/lang/reflect/Proxy.html.
    List<Method> methods = Arrays.asList(klass.getMethods());
 
    if (methods.isEmpty()) {
      throw new IllegalArgumentException(klass.getName() + " has not methods.");
    }
 
    ProfilingMethodInterceptor pmi = new ProfilingMethodInterceptor(clock, state, delegate);
 
    return (T) Proxy.newProxyInstance(ProfilerImpl.class.getClassLoader(), new Class[] {klass}, pmi);
  }
 
  /**
   * Returns the {@link ProfilingState} object that this class uses to track profiling data.
   */
  @Override
  public void writeData(Path path) throws IOException {
    // Write the ProfilingState data to the given file path. If a file already exists at that
    //       path, the new data should be appended to the existing file.
    Objects.requireNonNull(path);
 
    try (Writer writer = Files.newBufferedWriter(path)) {
      writeData(writer);
      writer.flush();
    } catch (IOException ex) {
      throw new IOException("Failed to write data to path: " + path, ex);
    }
  }
  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
