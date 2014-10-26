/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.publicalerts.cap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.publicalerts.cap.Reason.Level;

import java.util.Iterator;
import java.util.List;

/**
 * A simple collection of {@link Reason} objects. The content is indexed to
 * allow for fast lookups based on the reasons' levels.
 * 
 * <p>Insertion order is not preserved, duplicate objects are not overwritten.
 * 
 * <p>This class is immutable and thread-safe.
 * 
 * @author sschiavoni@google.com (Stefano Schiavoni)
 */
public final class Reasons implements Iterable<Reason> {
  public static final Reasons EMPTY = Reasons.of();
  
  private final ImmutableListMultimap<Level, Reason> multimap;
  
  /**
   * Builder for {@link Reasons}.
   */
  public static class Builder {
    private final ImmutableListMultimap.Builder<Level, Reason>
        multimapBuilder = ImmutableListMultimap.builder();

    /**
     * Use {@link Reasons#newBuilder()} instead.
     */
    private Builder() { }
    
    public Builder add(Reason reason) {
      multimapBuilder.put(reason.getLevel(), reason);
      return this;
    }
    
    public Builder addAll(Iterable<Reason> reasons) {
      for (Reason reason : reasons) {
        add(reason);
      }

      return this;
    }
    
    public Reasons build() {
      return new Reasons(multimapBuilder.build());
    }
  }

  /**
   * Creates an object of type {@link Reasons.Builder}.
   */
  public static Reasons.Builder newBuilder() {
    return new Reasons.Builder();
  }

  /**
   * Creates a {@link Reasons} object with the content specified.
   */
  public static Reasons of(Reason... reasons) {
    Reasons.Builder builder = Reasons.newBuilder();
    
    for (Reason reason : reasons) {
      builder.add(reason);
    }
    
    return builder.build();
  }
  
  /**
   * Builds a copy of the {@link Reasons} provided as input, with the XPath
   * prefixed with {@code xPathPrefix}.
   * 
   * @see Reason#prefixWithXpath(Reason, String)
   */
  public static Reasons prefixWithXpath(Reasons reasons, String xPathPrefix) {
    Reasons.Builder builder = Reasons.newBuilder();
    
    for (Reason reason : reasons) {
      builder.add(Reason.prefixWithXpath(reason, xPathPrefix));
    }

    return builder.build();
  }
  
  /**
   * Private constructor.
   * 
   * <p>Build {@link Reasons} objects using the static
   * {@link Reasons#of(Reason...)} or {@link Reasons.Builder}.
   */  
  private Reasons(ImmutableListMultimap<Level, Reason> multimap) {
    this.multimap = multimap;
  }
  
  @Override
  public Iterator<Reason> iterator() {
    return multimap.values().iterator();
  }

  /**
   * @return {@code true} if and only if the collection contains {@link Reason}
   * objects with the level specified
   */
  public boolean containsWithLevel(Level level) {
    return !getWithLevel(level).isEmpty();
  }
  
  /**
   * @return {@code true} if and only if the collection contains {@link Reason}
   * objects with the level specified or higher
   */
  public boolean containsWithLevelOrHigher(Level level) {
    return !getWithLevelOrHigher(level).isEmpty();
  }
  
  /**
   * @return the {@link Reason} objects stored in the collection, with the
   * level specified
   */
  public List<Reason> getWithLevel(Level level) {
    return multimap.get(level);
  }
  
  /**
   * @return the {@link Reason} objects stored in the collection, with the
   * level specified or higher
   */
  public List<Reason> getWithLevelOrHigher(Level level) {
    ImmutableList.Builder<Reason> list = ImmutableList.builder();
    
    list.addAll(getWithLevel(level));
    
    for (Level higherLevel : level.getHigherLevels()) {
      list.addAll(getWithLevel(higherLevel));
    }
    
    return list.build();
  }
}
