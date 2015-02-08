/*
 * Copyright (c) 2015. David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package uk.q3c.krail.core.user;

import uk.q3c.krail.core.user.status.UserStatusListener;

/**
 * Used to identify the source of a login - usually a View implementation, component or possibly even a federated
 * source.  Enables {@link UserStatusListener}s to respond differently depending on the source of the change
 * <p>
 * Created by David Sowerby on 08/02/15.
 */
public interface UserStatusChangeSource {

    default String identity() {
        return getClass().getSimpleName();
    }
}
