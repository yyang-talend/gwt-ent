/*
 * GwtEnt - Gwt ent library.
 * 
 * Copyright (c) 2007, James Luo(JamesLuo.au@gmail.com)
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.gwtent.client.reflection.impl;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gwtent.client.reflection.AnnotationStore;
import com.gwtent.client.reflection.ClassType;
import com.gwtent.client.reflection.HasAnnotations;
import com.gwtent.client.reflection.NotFoundException;
import com.gwtent.client.reflection.Package;


public class PackageImpl implements HasAnnotations, Package {

  private final Annotations annotations = new Annotations();
	  private final String name;

	  private final Map types = new HashMap();

	  public PackageImpl(String name) {
	    this.name = name;
	  }

	  /* (non-Javadoc)
	 * @see com.gwtent.client.reflection.Package#findType(java.lang.String)
	 */
	public ClassType findType(String typeName) {
	    String[] parts = typeName.split("\\.");
	    return findType(parts);
	  }

	  /* (non-Javadoc)
	 * @see com.gwtent.client.reflection.Package#findType(java.lang.String[])
	 */
	public ClassType findType(String[] typeName) {
	    return findTypeImpl(typeName, 0);
	  }

	  /* (non-Javadoc)
	 * @see com.gwtent.client.reflection.Package#getName()
	 */
	public String getName() {
	    return name;
	  }

	  /* (non-Javadoc)
	 * @see com.gwtent.client.reflection.Package#getType(java.lang.String)
	 */
	public ClassType getType(String typeName) {
	    ClassType result = findType(typeName);
	    if (result == null) {
	      throw new NotFoundException();
	    }
	    return result;
	  }

	  /* (non-Javadoc)
	 * @see com.gwtent.client.reflection.Package#getTypes()
	 */
	public ClassType[] getTypes() {
	    return (ClassType[]) types.values().toArray(TypeOracleImpl.NO_JCLASSES);
	  }

	  /* (non-Javadoc)
	 * @see com.gwtent.client.reflection.Package#isDefault()
	 */
	public boolean isDefault() {
	    return "".equals(name);
	  }

	  public String toString() {
	    return "package " + name;
	  }

	  void addType(ClassTypeImpl type) {
	    types.put(type.getSimpleSourceName(), type);
	  }

	  ClassType findTypeImpl(String[] typeName, int index) {
	    ClassTypeImpl found = (ClassTypeImpl) types.get(typeName[index]);
	    if (found == null) {
	      return null;
	    } else if (index < typeName.length - 1) {
	      return found.findNestedTypeImpl(typeName, index + 1);
	    } else {
	      return found;
	    }
	  }

	  void remove(ClassTypeImpl type) {
	    Object removed = types.remove(type.getSimpleSourceName());
	    // JDT will occasionally remove non-existent items, such as packages.
	  }
	  
	  public void addAnnotations(
	      List<AnnotationStore> annotations) {
	    this.annotations.addAnnotations(annotations);
	  }
	  
	  public <T extends Annotation> AnnotationStore getAnnotation(Class<T> annotationClass) {
	    return annotations.getAnnotation(annotationClass);
	  }
	  
	  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
	    return annotations.isAnnotationPresent(annotationClass);
	  }
	  
	  /**
	   * NOTE: This method is for testing purposes only.
	   */
	  AnnotationStore[] getAnnotations() {
	    return annotations.getAnnotations();
	  }

	  /**
	   * NOTE: This method is for testing purposes only.
	   */
	  AnnotationStore[] getDeclaredAnnotations() {
	    return annotations.getDeclaredAnnotations();
	  }
}
