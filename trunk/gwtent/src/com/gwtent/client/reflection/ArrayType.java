/*
 * Copyright 2007 Google Inc.
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
package com.gwtent.client.reflection;

/**
 * Type representing a Java array.
 */
public class ArrayType extends Type {

  private Type componentType;

  private String lazyQualifiedName;

  private String lazySimpleName;

  ArrayType(Type componentType) {
    this.componentType = componentType;
  }

  public Type getComponentType() {
    return componentType;
  }

  public String getJNISignature() {
    return "[" + componentType.getJNISignature();
  }

  public Type getLeafType() {
    return componentType.getLeafType();
  }

  public String getParameterizedQualifiedSourceName() {
    return getComponentType().getParameterizedQualifiedSourceName() + "[]";
  }

  public String getQualifiedSourceName() {
    if (lazyQualifiedName == null) {
      lazyQualifiedName = getComponentType().getQualifiedSourceName() + "[]";
    }
    return lazyQualifiedName;
  }

  public int getRank() {
    ArrayType componentArrayType = componentType.isArray();
    if (componentArrayType != null) {
      return 1 + componentArrayType.getRank();
    }

    return 1;
  }

  public String getSimpleSourceName() {
    if (lazySimpleName == null) {
      lazySimpleName = getComponentType().getSimpleSourceName() + "[]";
    }
    return lazySimpleName;
  }

  public ArrayType isArray() {
    return this;
  }

  public ClassType isClass() {
    // intentional null
    return null;
  }

  public ClassType isInterface() {
    // intentional null
    return null;
  }

//  public ParameterizedType isParameterized() {
//    // intentional null
//    return null;
//  }

  public PrimitiveType isPrimitive() {
    // intentional null
    return null;
  }

  public void setLeafType(Type type) {
    ArrayType componentTypeIsArray = componentType.isArray();
    if (componentTypeIsArray != null) {
      componentTypeIsArray.setLeafType(type);
    } else {
      componentType = type;
    }
  }

  public String toString() {
    return getQualifiedSourceName();
  }
}
