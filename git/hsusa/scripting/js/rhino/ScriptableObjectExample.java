/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package git.hsusa.scripting.js.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.Scriptable;

/**
 * A bare-bones-example-native object with constructor using
 * ScriptableObjectKit.
 * @author pc.wiz.tt
 * @see ScriptableObjectExample#init
 * @see ScriptableObjectExample#getScriptableInstanceValue
 * @see ScriptableObjectExample#constructor
 * @see ScriptableObjectExample#ScriptableObjectExample
 * 
 * 
 */
public class ScriptableObjectExample extends ScriptableObjectKit
{
      /**
       * The JavaScript name of this Class Constructor
       */
    public static final String CLASS_NAME = "ScriptableObjectExample";
   
    /* local data here */
    
    /**
     * Install ScriptableObjectExample constructor in scope.
     * @param cx
     * @param scope
     * @param sealed 
     */
    public static void init(Context cx, Scriptable scope, boolean sealed)
    {
        // use a ScriptableObjectExample dummy object.
        ScriptableObjectExample na = new ScriptableObjectExample();
        
        // let's get robo-scriptable-id-ing.
        na.bootClass(ScriptableObjectExample.class, CLASS_NAME);

        // define some robo-method-names
        String[] names = {
          "constructor", // you can add more methods
        };
        
        // add your robo-properties here
        // ... addScriptInstance(...)
        
        na.addScriptMethods(names, 0); // registers this method with zero arity.
        // you could call addScriptMethods again with a different list and
        // arity...
        
        // finalize
        na.exportAsJSClass(na.getMaxPrototypeId(), scope, sealed);
        
        // don't worry about booting more than once.
        // that issue is covered for you. addScriptInstance calls need to setup
        // instance values, so the already booted status is honored for method
        // settings.
        
    }

    /**
     * Create an empty example.
     * or,... the script runtime-logic won't like you.
     */
    public ScriptableObjectExample()
    {
      // initialize your defaults, etc..
    }

    /**
     * Bare-bones-constructor
     * @param cx
     * @param thisObj
     * @param args
     * @param f
     * @return Your new Object()
     */
    public ScriptableObjectExample constructor(Context cx,
      Scriptable thisObj, Object[] args, IdFunctionObject f)
    {
      return new ScriptableObjectExample();
    }
    
  @Override
  public <ANY> ANY getScriptableInstanceValue(InstanceValue inst) {
        
    return (ANY) inst.value; 
    
  }

}
