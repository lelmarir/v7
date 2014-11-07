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
package uk.co.q3c.v7.base.navigate;

import uk.co.q3c.v7.base.user.notify.UserNotifier;
import uk.co.q3c.v7.base.user.notify.UserNotifier.NotificationType;
import uk.co.q3c.v7.i18n.MessageKey;

import com.google.inject.Inject;

public class DefaultInvalidURIExceptionHandler implements InvalidURIExceptionHandler {

	private final UserNotifier notifier;
	private final V7Navigator navigator;

	@Inject
	protected DefaultInvalidURIExceptionHandler(UserNotifier notifier, V7Navigator navigator){
		this.notifier = notifier;
		this.navigator = navigator;
	}

	@Override
	public void onInvalidUri(InvalidURIException error) {
		notifier.show(NotificationType.ERROR, MessageKey.invalidURI, error.getTargetURI());
		navigator.updateUriFragment();
	}

}
