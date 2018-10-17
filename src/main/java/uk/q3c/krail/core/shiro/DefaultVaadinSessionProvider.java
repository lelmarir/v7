/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.q3c.krail.core.shiro;

import com.vaadin.server.VaadinSession;

/**
 * @author David Sowerby 15 Sep 2013
 * @see VaadinSessionProvider
 */
public class DefaultVaadinSessionProvider implements VaadinSessionProvider {
	@Override
	public VaadinSession get() {
		return VaadinSession.getCurrent();
	}

}
