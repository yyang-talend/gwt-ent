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
package com.gwtent.client.ui.transition;

import com.gwtent.client.reflection.impl.ClassTypeImpl;
import com.gwtent.client.ui.model.Domain;

public interface POJOToModel {
	/**
	 * For maximum efficiency, this need ClassType param
	 * use must invoke GWT.create()
	 * need a Class param
	 * @param pojo
	 * @param clasz
	 * @return
	 * @throws TransitionException
	 */
	public Domain createModel(Object pojo, ClassTypeImpl classType) throws TransitionException;
}
