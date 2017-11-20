/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package git.hsusa.scripting.js.rhino;

import git.hsusa.scripting.js.rhino.ScriptableObjectKit.InstanceValue;

/**
 * Instance Value Getter
 * @author pc.wiz.tt (& co.)
 * @see ScriptableInstanceValueGetter#getScriptableInstanceValue
 * @see ScriptableObjectKit
 * @see ScriptableObjectExample
 */
public interface ScriptableInstanceValueGetter {
  /**
   *  Typically, you convert inst.value,
   *  and return the conversion to javascript if it isn't already.
   *<p> <code>
   *  return (ANY) ScriptRuntime.toInt32(inst.value); // for example
   * </code></p><br>
   *  <cite>InstanceValue inst</cite> is guaranteed to be non-null.
   * @param <ANY>
   * @param inst
   * @return null to allow the request to bubble IF NEEDED  @Override
   */
    <ANY> ANY getScriptableInstanceValue(InstanceValue inst);
}
