package com.hdu.fact.async.util;

import com.hdu.fact.async.annotation.Async;
import com.hdu.fact.async.exception.AsyncException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 *
 *
 * </p>
 *
 * @author zhou
 */
public final class ReflectionHelper {
    private final static Logger logger = LoggerFactory.getLogger(ReflectionHelper.class);

    private static final Map primitiveValueMap = new HashMap(16);

    static {
        primitiveValueMap.put(Boolean.class, Boolean.FALSE);
        primitiveValueMap.put(Byte.class, Byte.valueOf((byte) 0));
        primitiveValueMap.put(Character.class, Character.valueOf((char) 0));
        primitiveValueMap.put(Short.class, Short.valueOf((short) 0));
        primitiveValueMap.put(Double.class, Double.valueOf(0));
        primitiveValueMap.put(Float.class, Float.valueOf(0));
        primitiveValueMap.put(Integer.class, Integer.valueOf(0));
        primitiveValueMap.put(Long.class, Long.valueOf(0));
        primitiveValueMap.put(boolean.class, Boolean.FALSE);
        primitiveValueMap.put(byte.class, Byte.valueOf((byte) 0));
        primitiveValueMap.put(char.class, Character.valueOf((char) 0));
        primitiveValueMap.put(short.class, Short.valueOf((short) 0));
        primitiveValueMap.put(double.class, Double.valueOf(0));
        primitiveValueMap.put(float.class, Float.valueOf(0));
        primitiveValueMap.put(int.class, Integer.valueOf(0));
        primitiveValueMap.put(long.class, Long.valueOf(0));
    }

    public static Object newInstance(Class type) {
        Constructor constructor = null;
        Object[] constructorArgs = new Object[0];
        try {
            // 先尝试默认的空构造函数
            constructor = type.getConstructor(new Class[]{});
        } catch (NoSuchMethodException e) {
            // ignore
        }

        // 没有默认的构造函数，尝试别的带参数的函数
        if (constructor == null) {
            Constructor[] constructors = type.getConstructors();
            if (constructors == null || constructors.length == 0) {
                throw new UnsupportedOperationException("Class[" + type.getName() + "] has no public constructors");
            }
            // 默认取第一个参数
            constructor = constructors[getSimpleParamenterTypeIndex(constructors)];
            Class[] params = constructor.getParameterTypes();
            constructorArgs = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                constructorArgs[i] = getDefaultValue(params[i]);
            }
        }
        return ReflectUtils.newInstance(constructor, constructorArgs);
    }

    public static int getSimpleParamenterTypeIndex(Constructor[] constructors) {
        Constructor constructor = null;
        Class[] params = null;
        boolean isSimpleTypes;
        for (int i = 0; i < constructors.length; i++) {
            constructor = constructors[i];
            params = constructor.getParameterTypes();
            if (params.length > 0) {
                isSimpleTypes = true;
                for (int j = 0; j < params.length; j++) {
                    if (primitiveValueMap.get(params[j]) == null) {
                        isSimpleTypes = false;
                        break;
                    }
                }
                if (isSimpleTypes) {
                    return i;
                }
            } else {
                return i;
            }
        }
        return 0;
    }

    public static Object getDefaultValue(Class cl) {
        // 处理数组
        if (cl.isArray()) {
            return Array.newInstance(cl.getComponentType(), 0);
        } else if (cl.isPrimitive() || primitiveValueMap.containsKey(cl)) {
            // 处理原型
            return primitiveValueMap.get(cl);
        } else {
            return null;
        }
    }

    public static Object invoke(Object obj, Object[] finArgs, Method method) throws Throwable {
        if (method == null) {
            return null;
        }
        try {
            logger.info("===== method.name: {}", method.getName());
            return method.invoke(obj, finArgs);
        } catch (IllegalAccessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e.getTargetException() != null ? e.getTargetException() : e;
        }
    }


    public static Class<?> getGenericClass(Class<?> cls) {
        return getGenericClass(cls, 0);
    }


    public static Class<?> getGenericClass(Class<?> cls, int i) {
        Type type = cls.getGenericSuperclass();
        /**
         * getGenericSuperclass()获得带有泛型的父类
         * Type是 Java 编程语言中所有类型的公共高级接口。它们包括原始类型、参数化类型、数组类型、类型变量和基本类型。
         */
        if (!(type instanceof ParameterizedType)) {
            throw new AsyncException("you should specify <?> for type");
        }
        return getGenericClass((ParameterizedType) type, i);
    }

    public static Class<?> getGenericClass(ParameterizedType parameterizedType, int i) {
        /**getActualTypeArguments获取参数化类型的数组，泛型可能有多个*/
        Object genericClass = parameterizedType.getActualTypeArguments()[i];
        // 处理多级泛型
        if (genericClass instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genericClass).getRawType();
        }
        // 处理数组泛型
        else if (genericClass instanceof GenericArrayType) {
            return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
        }
        // 处理泛型擦拭对象
        else if (genericClass instanceof TypeVariable) {
            return (Class) getClass(((TypeVariable) genericClass).getBounds()[0], 0);
        } else {
            return (Class<?>) genericClass;
        }
    }

    private static Class getClass(Type type, int i) {
        // 处理泛型类型
        if (type instanceof ParameterizedType) {
            return getGenericClass((ParameterizedType) type, i);
        } else if (type instanceof TypeVariable) {
            // 处理泛型擦拭对象
            return (Class) getClass(((TypeVariable) type).getBounds()[0], 0);
        } else { // class本身也是type，强制转型
            return (Class) type;
        }
    }

    /**
     * 发现被@Async注解标注的方法
     * @param bean
     * @param method
     */
    public static Async findAsyncAnnotation(Object bean, Method method) {
        Async classAnnotation = AnnotationUtils.findAnnotation(bean.getClass(), Async.class);
        Async methodAnnotation = AnnotationUtils.findAnnotation(method, Async.class);
        if (methodAnnotation != null || classAnnotation != null) {
            Class<?> returnClass = method.getReturnType();
            if (Void.TYPE.isAssignableFrom(returnClass) || canProxy(returnClass)) {
                return methodAnnotation;
            }
            return classAnnotation;
        }
        return null;
    }

    /**
     * 判断是否可以将该类代理
     * @param cls
     */
    public static boolean canProxy(Class cls) {
        if (Void.class.equals(cls) || Void.TYPE.isAssignableFrom(cls)
                || !Modifier.isPublic(cls.getModifiers()) || Modifier.isFinal(cls.getModifiers())
                || cls.isArray() || cls.isPrimitive() || cls == Object.class) {
            return false;
        }
        return true;
    }

    /**
     * 判断该方法能否执行
     * @param method
     * @return
     */
    public static boolean canProxyInvoke(Method method) {
        if ("finalize".equals(method.getName())) {
            return false;
        }
        return true;
    }

    public static boolean isVoid(Method method) {
        if (method == null) {
            return false;
        }
        return (Void.TYPE.isAssignableFrom(method.getReturnType()) || Void.class.equals(method.getReturnType()));
    }

    public static Throwable getThrowableCause(Throwable e) {
        if (e == null || e.getCause() == null) {
            return e;
        } else {
            Throwable throwable = getThrowableCause(e.getCause());
            if (throwable == null) {
                return e;
            } else {
                return throwable;
            }
        }
    }

}
