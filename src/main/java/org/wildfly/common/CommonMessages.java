/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.common;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
@MessageBundle(projectCode = "COM", length = 5)
interface CommonMessages {
    CommonMessages msg = Messages.getBundle(CommonMessages.class);

    // simple validation

    @Message(id = 0, value = "Parameter '%s' may not be null")
    IllegalArgumentException nullParam(String paramName);

    // assertion errors

    @Message(id = 1000, value = "Internal error: Assertion failure: Unexpectedly null value")
    String unexpectedNullValue();

    @Message(id = 1001, value = "Internal error: Assertion failure: Current thread expected to hold lock for %s")
    String expectedLockHold(Object monitor);

    @Message(id = 1002, value = "Internal error: Assertion failure: Current thread expected to not hold lock for %s")
    String expectedLockNotHold(Object monitor);

    // internal state errors

    @Message(id = 2000, value = "Internal error: Unreachable code has been reached")
    IllegalStateException unreachableCode();

    @Message(id = 2001, value = "Internal error: Impossible switch condition encountered: %s")
    IllegalStateException impossibleSwitchCase(Object cond);

    // 3000-3099 reserved for reference queue logging

}
