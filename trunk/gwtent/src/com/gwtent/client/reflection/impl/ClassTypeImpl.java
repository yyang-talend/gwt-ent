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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gwtent.client.reflection.AccessDef;
import com.gwtent.client.reflection.AnnotationStore;
import com.gwtent.client.reflection.ArrayType;
import com.gwtent.client.reflection.ClassType;
import com.gwtent.client.reflection.Field;
import com.gwtent.client.reflection.HasAnnotations;
import com.gwtent.client.reflection.HasMetaData;
import com.gwtent.client.reflection.Method;
import com.gwtent.client.reflection.NotFoundException;
import com.gwtent.client.reflection.Package;
import com.gwtent.client.reflection.Parameter;
import com.gwtent.client.reflection.PrimitiveType;
import com.gwtent.client.reflection.Type;

/**
 * Type representing a Java class or interface type.
 */
public class ClassTypeImpl extends TypeImpl implements HasMetaData, AccessDef, HasAnnotations, ClassType {

	private final Set<ClassTypeImpl> allSubtypes = new HashSet<ClassTypeImpl>();
	private final Annotations annotations = new Annotations();

	private Method[] cachedOverridableMethods;

	private final List<ConstructorImpl> constructors = new ArrayList<ConstructorImpl>();

	private ClassTypeImpl enclosingType;

	private final Map<String, FieldImpl> fields = new HashMap<String, FieldImpl>();

	private final List<ClassTypeImpl> interfaces = new ArrayList<ClassTypeImpl>();

	private boolean isInterface = false;

	private boolean isLocalType = true;

	//private String lazyHash;

	private String lazyQualifiedName;

	private final Map methods = new HashMap<String, List>();

	private int modifierBits;

	private String name;

	private String nestedName;

	private final Map nestedTypes = new HashMap();

	private ClassTypeImpl superclass;

	private Package declaringPackage;

	private final HasMetaData metaData = new MetaData();

	private boolean savedIsDefaultInstantiable;

	public Object invoke(Object instance, String methodName, Object[] args) {
		throw new NotFoundException(methodName + "not found or unimplement?");
	}

	public ClassTypeImpl(String qualifiedName) {
		TypeOracleImpl.putType(this, qualifiedName);
	}

	/**
	 * NOT USE at this moment, we need refactor here
	 * 
	 * @param declaringPackage
	 * @param enclosingType
	 * @param isLocalType
	 * @param name
	 * @param isInterface
	 * @param isDefaultInstantiable
	 */
	public ClassTypeImpl(PackageImpl declaringPackage, ClassTypeImpl enclosingType,
			boolean isLocalType, String name, boolean isInterface,
			boolean isDefaultInstantiable) {

		this.declaringPackage = declaringPackage;
		this.enclosingType = enclosingType;
		this.isLocalType = isLocalType;
		this.name = name;
		this.savedIsDefaultInstantiable = isDefaultInstantiable;

		this.isInterface = isInterface;
		if (enclosingType == null) {
			// Add myself to my package.
			//
			declaringPackage.addType(this);
			// The nested name of a top-level class is its simple name.
			//
			nestedName = name;
		} else {
			// Add myself to my enclosing type.
			//
			enclosingType.addNestedType(this);
			// Compute my "nested name".
			//
			ClassTypeImpl enclosing = enclosingType;
			String nn = name;
			do {
				nn = enclosing.getSimpleSourceName() + "." + nn;
				enclosing = enclosing.getEnclosingType();
			} while (enclosing != null);
			nestedName = nn;
		}
	}

	public void addImplementedInterface(ClassTypeImpl intf) {
//		assert (intf != null);
		interfaces.add(intf);
	}

	public void addModifierBits(int bits) {
		modifierBits |= bits;
	}

	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#findField(java.lang.String)
	 */
	public FieldImpl findField(String name) {
		return (FieldImpl) fields.get(name);
	}

	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#findMethod(java.lang.String, com.gwtent.client.reflection.Type[])
	 */
	public MethodImpl findMethod(String name, Type[] paramTypes) {
		MethodImpl[] overloads = getOverloads(name);
		for (int i = 0; i < overloads.length; i++) {
			MethodImpl candidate = overloads[i];
			if (candidate.hasParamTypes(paramTypes)) {
				return candidate;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#findMethod(java.lang.String, java.lang.String[])
	 */
	public MethodImpl findMethod(String name, String[] paramTypes) {
		MethodImpl[] overloads = getOverloads(name);
		for (int i = 0; i < overloads.length; i++) {
			MethodImpl candidate = overloads[i];
			if (candidate.hasParamTypesByTypeName(paramTypes)) {
				return candidate;
			}
		}
		return null;
	}

	public ClassType findNestedType(String typeName) {
		String[] parts = typeName.split("\\.");
		return findNestedTypeImpl(parts, 0);
	}

	public ClassTypeImpl getEnclosingType() {
		return enclosingType;
	}

	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#getField(java.lang.String)
	 */
	public Field getField(String name) {
		Field field = findField(name);
//		assert (field != null);
		return field;
	}

	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#getFields()
	 */
	public FieldImpl[] getFields() {
		return (FieldImpl[]) fields.values().toArray(TypeOracleImpl.NO_JFIELDS);
	}

	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#getImplementedInterfaces()
	 */
	public ClassTypeImpl[] getImplementedInterfaces() {
		return (ClassTypeImpl[]) interfaces.toArray(TypeOracleImpl.NO_JCLASSES);
	}

	public String getJNISignature() {
		String typeName = nestedName.replace('.', '$');
		String packageName = getPackage().getName().replace('.', '/');
		if (packageName.length() > 0) {
			packageName += "/";
		}
		return "L" + packageName + typeName + ";";
	}

	/**
	 * Manages doc comment metadata for an AST item. The structure of the metadata
	 * attempts to mirror the way in which tags and values were originally declared.
	 * 
	 * <p>
	 * For example, for the following declaration
	 * 
	 * <pre>
	 * /**
	 *  * @myTag value1 value2
	 *  * @myTag value3 value4
	 *  * ... 
	 * </pre>
	 * 
	 * a call to <code>getMetaData("myTag")</code> would return this array of
	 * string arrays
	 * 
	 * <pre>
	 *[0][0] = value1
	 *[0][1] = value2
	 *[1][0] = value3
	 *[1][1] = value4
	 * </pre>
	 * 
	 * </p>
	 */
	public String[][] getMetaData(String tagName) {
		return metaData.getMetaData(tagName);
	}
	
	public List getMetaDataMerge(String tagName){
		String[][] metaDatas = getMetaData(tagName);
		List result = new ArrayList();
		for (int i = 0; i < metaDatas.length; i++){
			for (int j = 0; j < metaDatas[i].length; j++){
				result.add(metaDatas[i][j]);
			}
		}
		return result;
	}

	public String[] getMetaDataTags() {
		return metaData.getMetaDataTags();
	}

	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#getMethod(java.lang.String, com.gwtent.client.reflection.Type[])
	 */
	public Method getMethod(String name, Type[] paramTypes)
			throws NotFoundException {
		Method result = findMethod(name, paramTypes);
		if (result == null) {
			throw new NotFoundException();
		}
		return result;
	}

	/*
	 * Returns the declared methods of this class (not any superclasses or
	 * superinterfaces).
	 */
	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#getMethods()
	 */
	public MethodImpl[] getMethods() {
		List resultMethods = new ArrayList();
		for (Iterator iter = methods.values().iterator(); iter.hasNext();) {
			List overloads = (List) iter.next();
			resultMethods.addAll(overloads);
		}
		return (MethodImpl[]) resultMethods.toArray(TypeOracleImpl.NO_JMETHODS);
	}

	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#getName()
	 */
	public String getName() {
		return nestedName;
	}

	public ClassType getNestedType(String typeName) throws NotFoundException {
		ClassType result = findNestedType(typeName);
		if (result == null) {
			throw new NotFoundException();
		}
		return result;
	}

	public ClassType[] getNestedTypes() {
		return (ClassType[]) nestedTypes.values().toArray(
				TypeOracleImpl.NO_JCLASSES);
	}

	public MethodImpl[] getOverloads(String name) {
		List resultMethods = (List) methods.get(name);
		if (resultMethods != null) {
			return (MethodImpl[]) resultMethods.toArray(TypeOracleImpl.NO_JMETHODS);
		} else {
			return TypeOracleImpl.NO_JMETHODS;
		}
	}

	/**
	 * Iterates over the most-derived declaration of each unique overridable
	 * method available in the type hierarchy of the specified type, including
	 * those found in superclasses and superinterfaces. A method is overridable
	 * if it is not <code>final</code> and its accessibility is
	 * <code>public</code>, <code>protected</code>, or package protected.
	 * 
	 * Deferred binding generators often need to generate method
	 * implementations; this method offers a convenient way to find candidate
	 * methods to implement.
	 * 
	 * Note that the behavior does not match
	 * {@link Class#getMethod(String, Class[])}, which does not return the most
	 * derived method in some cases.
	 * 
	 * @return an array of {@link MethodImpl} objects representing overridable
	 *         methods
	 */
	public Method[] getOverridableMethods() {
		if (cachedOverridableMethods == null) {
			Map methodsBySignature = new HashMap();
			getOverridableMethodsOnSuperinterfacesAndMaybeThisInterface(methodsBySignature);
			if (isClass() != null) {
				getOverridableMethodsOnSuperclassesAndThisClass(methodsBySignature);
			}
			int size = methodsBySignature.size();
			Collection leafMethods = methodsBySignature.values();
			cachedOverridableMethods = (Method[]) leafMethods
					.toArray(new Method[size]);
		}
		return cachedOverridableMethods;
	}

	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#getPackage()
	 */
	public Package getPackage() {
		return declaringPackage;
	}

	public String getQualifiedSourceName() {
		if (lazyQualifiedName == null) {
			Package pkg = getPackage();
			if (!pkg.isDefault()) {
				lazyQualifiedName = pkg.getName() + "."
						+ makeCompoundName(this);
			} else {
				lazyQualifiedName = makeCompoundName(this);
			}
		}
		return lazyQualifiedName;
	}

	public String getSimpleSourceName() {
		return name;
	}

	public ClassType[] getSubtypes() {
		return (ClassType[]) allSubtypes.toArray(TypeOracleImpl.NO_JCLASSES);
	}

	/* (non-Javadoc)
	 * @see com.gwtent.client.reflection.ClassType#getSuperclass()
	 */
	public ClassTypeImpl getSuperclass() {
		return superclass;
	}

	public boolean isAbstract() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_ABSTRACT);
	}

	public ArrayType isArray() {
		// intentional null
		return null;
	}

	public boolean isAssignableFrom(ClassType possibleSubtype) {
		if (possibleSubtype == this) {
			return true;
		}
		if (allSubtypes.contains(possibleSubtype)) {
			return true;
			// } else if (this == getOracle().getJavaLangObject()) {
			// // This case handles the odd "every interface is an Object"
			// // but doesn't actually have Object as a superclass.
			// //
			// return true;
		} else {
			return false;
		}
	}

	public boolean isAssignableTo(ClassTypeImpl possibleSupertype) {
		return possibleSupertype.isAssignableFrom(this);
	}

	public ClassType isClass() {
		return isInterface ? null : this;
	}

	/**
	 * Determines if the class can be constructed using a simple
	 * <code>new</code> operation. Specifically, the class must
	 * <ul>
	 * <li>be a class rather than an interface, </li>
	 * <li>have either no constructors or a parameterless constructor, and</li>
	 * <li>be a top-level class or a static nested class.</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if the type is default instantiable, or
	 *         <code>false</code> otherwise
	 */
	public boolean isDefaultInstantiable() {

		return savedIsDefaultInstantiable;
	}

	public ClassType isInterface() {
		return isInterface ? this : null;
	}

	/**
	 * Tests if this type is a local type (within a method).
	 * 
	 * @return true if this type is a local type, whether it is named or
	 *         anonymous.
	 */
	public boolean isLocalType() {
		return isLocalType;
	}

	/**
	 * Tests if this type is contained within another type.
	 * 
	 * @return true if this type has an enclosing type, false if this type is a
	 *         top-level type
	 */
	public boolean isMemberType() {
		return enclosingType != null;
	}

	// public JParameterizedType isParameterized() {
	// // intentional null
	// return null;
	// }
	//
	public PrimitiveType isPrimitive() {
		// intentional null
		return null;
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

	public void setSuperclass(ClassTypeImpl type) {
//		assert (type != null);
//		assert (isInterface() == null);
		this.superclass = type;
//		ClassType realSuperType;
//    if (type.isParameterized() != null) {
//      realSuperType = type.isParameterized().getBaseType();
//    } else if (type.isRawType() != null) {
//      realSuperType = type.isRawType().getGenericType();
//    } else {
//      realSuperType = (JRealClassType) type;
//    }
    annotations.setParent(type.annotations);
	}

	public String toString() {
		if (isInterface) {
			return "interface " + getQualifiedSourceName();
		} else {
			return "class " + getQualifiedSourceName();
		}
	}

	protected int getModifierBits() {
		return modifierBits;
	}

	protected void addField(FieldImpl field) {
		Object existing = fields.put(field.getName(), field);
		assert (existing == null);
	}

	public void addMethod(MethodImpl method) {
		String methodName = method.getName();
		List overloads = (List) methods.get(methodName);
		if (overloads == null) {
			overloads = new ArrayList();
			methods.put(methodName, overloads);
		}
		overloads.add(method);
	}

	void addNestedType(ClassTypeImpl type) {
		Object existing = nestedTypes.put(type.getSimpleSourceName(), type);
	}

	ClassType findNestedTypeImpl(String[] typeName, int index) {
		ClassTypeImpl found = (ClassTypeImpl) nestedTypes.get(typeName[index]);
		if (found == null) {
			return null;
		} else if (index < typeName.length - 1) {
			return found.findNestedTypeImpl(typeName, index + 1);
		} else {
			return found;
		}
	}

	void notifySuperTypes() {
		notifySuperTypesOf(this);
	}

	private void acceptSubtype(ClassTypeImpl me) {
		allSubtypes.add(me);
		notifySuperTypesOf(me);
	}

	private String computeInternalSignature(MethodImpl method) {
		StringBuffer sb = new StringBuffer();
		sb.setLength(0);
		sb.append(method.getName());
		Parameter[] params = method.getParameters();
		for (int j = 0; j < params.length; j++) {
			Parameter param = params[j];
			sb.append("/");
			sb.append(param.getType().getQualifiedSourceName());
		}
		return sb.toString();
	}

	private void getOverridableMethodsOnSuperclassesAndThisClass(
			Map methodsBySignature) {
//		assert (isClass() != null);

		// Recurse first so that more derived methods will clobber less derived
		// methods.
		ClassTypeImpl superClass = getSuperclass();
		if (superClass != null) {
			superClass
					.getOverridableMethodsOnSuperclassesAndThisClass(methodsBySignature);
		}

		MethodImpl[] declaredMethods = getMethods();
		for (int i = 0; i < declaredMethods.length; i++) {
			MethodImpl method = declaredMethods[i];

			// Ensure that this method is overridable.
			if (method.isFinal() || method.isPrivate()) {
				// We cannot override this method, so skip it.
				continue;
			}

			// We can override this method, so record it.
			String sig = computeInternalSignature(method);
			methodsBySignature.put(sig, method);
		}
	}

	/**
	 * Gets the methods declared in interfaces that this type extends. If this
	 * type is a class, its own methods are not added. If this type is an
	 * interface, its own methods are added. Used internally by
	 * {@link #getOverridableMethods()}.
	 * 
	 * @param methodsBySignature
	 */
	private void getOverridableMethodsOnSuperinterfacesAndMaybeThisInterface(
			Map methodsBySignature) {
		// Recurse first so that more derived methods will clobber less derived
		// methods.
		ClassTypeImpl[] superIntfs = getImplementedInterfaces();
		for (int i = 0; i < superIntfs.length; i++) {
			ClassTypeImpl superIntf = superIntfs[i];
			superIntf
					.getOverridableMethodsOnSuperinterfacesAndMaybeThisInterface(methodsBySignature);
		}

		if (isInterface() == null) {
			// This is not an interface, so we're done after having visited its
			// implemented interfaces.
			return;
		}

		MethodImpl[] declaredMethods = getMethods();
		for (int i = 0; i < declaredMethods.length; i++) {
			MethodImpl method = declaredMethods[i];

			String sig = computeInternalSignature(method);
			Method existing = (Method) methodsBySignature.get(sig);
			if (existing != null) {
				// ClassType existingType = existing.getEnclosingType();
				// ClassType thisType = method.getEnclosingType();
				// if (thisType.isAssignableFrom(existingType)) {
				// // The existing method is in a more-derived type, so don't
				// replace it.
				// continue;
				// }
			}
			methodsBySignature.put(sig, method);
		}
	}

	private String makeCompoundName(ClassTypeImpl type) {
		if (type.enclosingType == null) {
			return type.name;
		} else {
			return makeCompoundName(type.enclosingType) + "." + type.name;
		}
	}

	/**
	 * Tells this type's superclasses and superinterfaces about it.
	 */
	private void notifySuperTypesOf(ClassTypeImpl me) {
		if (superclass != null) {
			superclass.acceptSubtype(me);
		}
		for (int i = 0, n = interfaces.size(); i < n; ++i) {
			ClassTypeImpl intf = (ClassTypeImpl) interfaces.get(i);
			intf.acceptSubtype(me);
		}
	}

	public void addMetaData(String tagName, String[] values) {
		metaData.addMetaData(tagName, values);

	}

	public boolean isFinal() {
		return false;
	}

	void addConstructor(ConstructorImpl ctor) {
//		assert (!constructors.contains(ctor));
		constructors.add(ctor);
	}
	
  public void addAnnotations(
      List<AnnotationStore> declaredAnnotations) {
    annotations.addAnnotations(declaredAnnotations);
  }
  
  public <T extends Annotation> AnnotationStore getAnnotation(Class<T> annotationClass) {
    return annotations.getAnnotation(annotationClass);
  }
  
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return annotations.isAnnotationPresent(annotationClass);
  }
  
  AnnotationStore[] getAnnotations() {
    return annotations.getAnnotations();
  }

  AnnotationStore[] getDeclaredAnnotations() {
    return annotations.getDeclaredAnnotations();
  }

}
