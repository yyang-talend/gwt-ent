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
import java.util.List;
import java.util.Map;

import com.gwtent.client.reflection.AccessDef;
import com.gwtent.client.reflection.AnnotationStore;
import com.gwtent.client.reflection.ClassType;
import com.gwtent.client.reflection.Field;
import com.gwtent.client.reflection.HasAnnotations;
import com.gwtent.client.reflection.HasMetaData;
import com.gwtent.client.reflection.Type;


public class FieldImpl extends Field implements HasMetaData, AccessDef, HasAnnotations{


	private final ClassType enclosingType;
	
	private final Annotations annotations = new Annotations();

	  private int modifierBits;

	  private final String name;

	  //TODO not support yet(in ReflectionCreator), use typename insteed
	  private Type type;
	  private String typeName;
	  
	  private final HasMetaData metaData = new MetaData();

	  public FieldImpl(ClassTypeImpl enclosingType, String name) {
	    this.enclosingType = enclosingType;
	    this.name = name;

//	    assert (enclosingType != null);
	    enclosingType.addField(this);
	  }

	  public void addModifierBits(int modifierBits) {
	    this.modifierBits |= modifierBits;
	  }

	  /* (non-Javadoc)
	 * @see com.gwtent.client.reflection.Field#getEnclosingType()
	 */
	public ClassType getEnclosingType() {
	    return enclosingType;
	  }

	  /* (non-Javadoc)
	 * @see com.gwtent.client.reflection.Field#getName()
	 */
	public String getName() {
//	    assert (name != null);
	    return name;
	  }

	  /* (non-Javadoc)
	 * @see com.gwtent.client.reflection.Field#getType()
	 */
	public Type getType() {
//	    assert (type != null);
	    return type;
	  }

	  public boolean isDefaultAccess() {
	    return 0 == (modifierBits & (TypeOracleImpl.MOD_PUBLIC | TypeOracleImpl.MOD_PRIVATE | TypeOracleImpl.MOD_PROTECTED));
	  }

	  public boolean isFinal() {
	    return 0 != (modifierBits & TypeOracleImpl.MOD_FINAL);
	  }

	  public boolean isPrivate() {
	    return 0 != (modifierBits & TypeOracleImpl.MOD_PRIVATE);
	  }

	  public boolean isProtected() {
	    return 0 != (modifierBits & TypeOracleImpl.MOD_PROTECTED);
	  }

	  public boolean isPublic() {
	    return 0 != (modifierBits & TypeOracleImpl.MOD_PUBLIC);
	  }

	  public boolean isStatic() {
	    return 0 != (modifierBits & TypeOracleImpl.MOD_STATIC);
	  }

	  public boolean isTransient() {
	    return 0 != (modifierBits & TypeOracleImpl.MOD_TRANSIENT);
	  }

	  public boolean isVolatile() {
	    return 0 != (modifierBits & TypeOracleImpl.MOD_VOLATILE);
	  }

	  public void setType(Type type) {
	    this.type = type;
	  }

	public void addMetaData(String tagName, String[] values) {
		metaData.addMetaData(tagName, values);
		
	}

	public String[][] getMetaData(String tagName) {
		return metaData.getMetaData(tagName);
	}

	public String[] getMetaDataTags() {
		return metaData.getMetaDataTags();
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
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

  public void addAnnotations(
      List<AnnotationStore> annotations) {
    this.annotations.addAnnotations(annotations);
  }
}
