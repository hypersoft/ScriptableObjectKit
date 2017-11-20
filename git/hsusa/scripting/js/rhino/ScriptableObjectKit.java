/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package git.hsusa.scripting.js.rhino;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Scriptable;

/**
 * ScriptableObjectKit is a helper for creating Native Mozilla Rhino runtime objects.
 * This is not the most efficient form, but it is a sturdy working table,
 * however that suits your purposes.
 * <p>Compatibility: <cite>Rhino 1.7.7.1</cite></p>
 * @author pc.wiz.tt
 */
public abstract class ScriptableObjectKit extends IdScriptableObject 
  implements ScriptableInstanceValueGetter
{  private int max_prototype_id = 0;
  
  private boolean acceptingMethods = true;
  
  protected int getMaxPrototypeId() {
    acceptingMethods = false;
    return max_prototype_id;
  }
    
  private int getNextPrototypeId() {
    return ++max_prototype_id;
  }
  
  protected Method getMethodById(int id) {
    return method.get(id).method;
  }
  
  protected Method getMethodByName(String name) {
    return method.get(name).method;
  }

  protected void setMethodScope(Object name, Object scope) {
    if (! method.containsKey(name)) return;
    method.get(name).scope = scope;
  }
  
  private static class MethodHeader {
    Method method; Object scope;
    String name, realName;
    int id, arity;
    MethodHeader(int mhId, String mhName, Method mhMethod, int mhArity) {
      method = mhMethod; name = mhName; arity = mhArity;
      id = mhId;
    }
  }
  
  private Map<Object, MethodHeader> method = new HashMap();
  Method[] classMethods;
  String scriptingClassName;
  Class scriptingClassMain;

  @Override
  public String getClassName()
  {
      return scriptingClassName;
  }

  /**
   * Searches for the first method with the matching name.
   * @param name
   * @return the name matched or a definition exception.
   */
  protected Method getFirstClassMethod(String name) {
    for (Method m: classMethods)
      if (m.getName().equals(name)) return m;
    throw new RuntimeException("Construction for scriptable class method: "
      +name+" failed; name not found");
  }
  
  public ScriptableObjectKit() {}
  
  /**
   * Instantiate the scriptable object kit.
   * @param forClass
   * <p>The class hosting this kit instance.</p>
   * @param forName 
   * <p>The name you want your javascript class to be</p>
   */
  protected void bootClass(Class forClass, String forName) {
    if (! acceptingMethods) return;
    classMethods = forClass.getMethods();
    scriptingClassMain = forClass;
    scriptingClassName = forName;
  }
  
  /**
   * Provide an array of prototype/method names to associate with 0 arity.
   * The method names will be looked up, using java reflection,
   * and stored for later registration. Your java methods must have the correct
   * signature, call signatures will not be checked. You can not overload your
   * method; the correct the function signature is overloaded.
   * <H4>Java Method Signature:</H4>
   * <p><code>
   * &lt;ANY&gt; ANY M(Context cx, Scriptable thisObj, Object[] args, IdFunctionObject f);
   * </code></p>
   * @param names
   */
  protected void addScriptMethods(String[] names) {
    addScriptMethods(names, 0);
  }
  /**
   * Provide an array of prototype/method names to associate with an arity.
   * The method names will be looked up, using java reflection,
   * and stored for later registration. Your java methods must have the correct
   * signature, call signatures will not be checked. You can not overload your
   * method; the correct the function signature is overloaded.
   * 
   * <p>
   * Successive calls to this interface work the same way the first call works,
   * until <code>getMaxPrototypeId</code> is called for class registration.
   * </p>
   * <H4>Java Method Signature:</H4>
   * <p><code>
   * &lt;ANY&gt; ANY M(Context cx, Scriptable thisObj, Object[] args, IdFunctionObject f);
   * </code></p>
   * @param names
   * @param arity 
   */
  protected void addScriptMethods(String[] names, int arity) {
    if (!acceptingMethods) return;
    for (String mhName: names) {
      String realName = mhName;
      boolean aliased = false;
      if (mhName.contains(":")) {
        mhName = mhName.replaceAll(" ", "").replaceAll("\t", "");
        String[] mod = mhName.split(":");
        realName = mod[0];
        mhName = mod[1];
        aliased = true;
      }
      MethodHeader mH = new MethodHeader(
        getNextPrototypeId(),
        mhName, 
        getFirstClassMethod(realName),
        arity
      );
      mH.realName = realName;
      // map-lookup: scriptingClassName or id
      method.put(mhName, mH); method.put(mH.id, mH);
      if (aliased) method.put(realName, mH);
    } // it's a twister, a twister...
  }

  @Override // fire in the hole
  final protected void initPrototypeId(int id)
  {
      if (method.containsKey(id)) {
        MethodHeader nmr = method.get(id);
        initPrototypeMethod(scriptingClassName, nmr.id, nmr.name, nmr.arity);
      } else throw new IllegalArgumentException(String.valueOf(id));
  }
    
  /**
   * Lookup method id by name.
   * @param s
   * @return 
   */
  @Override // for the getting of the method/function id from string
  final protected int findPrototypeId(String s)
  {
    if (method.containsKey(s)) return method.get(s).id;
    return 0;
  }

  // for the completion of the script engine call forwarding
  private Object callMethodWithScriptArguments(Context cx, Scriptable thisObj,
    Object[] args, IdFunctionObject f)
  {
    MethodHeader vm = method.get(f.methodId());
    Method m = vm.method;
    Object out = null;
    try {
      out = m.invoke(this, cx, thisObj, args, f);
    } catch (IllegalAccessException ex) {
    } catch (IllegalArgumentException ex) {
    } catch (InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }
    return out;
  }

  // Function-calling dispatcher

  @Override // for the script engine forwarding of the script function call
  final public Object execIdCall(IdFunctionObject f, Context cx,
    Scriptable scope, Scriptable thisObj, Object[] args)
  {
      if (!f.hasTag(scriptingClassName)) {
          return super.execIdCall(f, cx, scope, thisObj, args);
      }
      return callMethodWithScriptArguments(cx, thisObj, args, f);
  }

      // Properties here

  public class InstanceValue { public String name; public int id; public Object value; public int security;}
  int max_property_id = 0;
  Map<Object, InstanceValue> properties = new HashMap();
  int getNextInstanceId() {return ++max_property_id;}
  
  /**
   * For adding your new instance values.
   * 
   * <p>You can use null values if you have the storage taken care of
   * elsewhere, and you just want the request signal when the kit calls
   * your overloaded: <code>getScriptableInstanceValue</code>
   * </p>
   * 
   * @param name whatever your property name is.
   * @param value whatever your initial value is.
   * @param security the rhino property attributes for this item.
   */
  protected void addScriptInstance(String name, Object value, int security) {
    InstanceValue p = new InstanceValue();
    p.id = getNextInstanceId();
    p.name = name;
    p.value = value;
    p.security = security;
    properties.put(name, p);
    properties.put(p.id, p);
  }

  /**
   * For your setting of an instance value.
   * @param key
   * @param value 
   */
  final protected void setInstanceValue(Object key, Object value) {
    if (! properties.containsKey(key)) return;
    properties.get(key).value = value;
  }
  /**
   * For your getting of the instance value by id or name.
   * @param <ANY>
   * @param key
   * @return InstanceValue or null
   */
  final protected <ANY> ANY getInstanceValue(Object key) {
    return (ANY) properties.get(key);
  }

  /**
   * For your discovery of the property id by name
   * @param name
   * @return property id
   */
  final protected int getInstanceNameId(String name) {
    return properties.get(name).id;
  }
  
  @Override // for the script engine enumerating the properties
  final protected int getMaxInstanceId()
  {
      return max_property_id;
  }

  @Override // for the script engine and your looking up a property name
  final protected String getInstanceIdName(int id)
  {
      if (properties.containsKey(id)) { return properties.get(id).name; }
      return super.getInstanceIdName(id);
  }
  
  @Override // for the script engine, getting your security config
  final protected int findInstanceIdInfo(String s)
    {
        if (properties.containsKey(s)) {
          InstanceValue pH = properties.get(s);
            return instanceIdInfo(pH.security, pH.id);
        }
        return super.findInstanceIdInfo(s);
    }

    @Override // for the script engine requesting your value id
   final protected Object getInstanceIdValue(int id)
    {
      InstanceValue iV = properties.get(id);
      Object data = null;
      if (iV != null) data = this.getScriptableInstanceValue(iV);
      if (data != null) return data;
      return super.getInstanceIdValue(id);
    }
}
