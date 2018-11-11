package io.netty.util.concurrent;

import io.netty.util.internal.InternalThreadLocalMap;
import io.netty.util.internal.PlatformDependent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class FastThreadLocal<V> {

    private final int index;

    /**
     * 当前FastThreadLocal实例所对应的索引
     * */
    public FastThreadLocal(){
        index = InternalThreadLocalMap.nextVariableIndex();
    }


    public final V get(){
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
        /**获取Object数组中当前索引对应的对象*/
        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            return (V) v;
        }
        V value = initialize(threadLocalMap);
        registerCleaner(threadLocalMap);
        return value;
    }

    /**
     * 返回当前线程映射值 如果不存在则将threadLocalMap设置为当前线程映射值
     * */
    @SuppressWarnings("unchecked")
    public final V get(InternalThreadLocalMap threadLocalMap) {
        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            return (V) v;
        }
        return initialize(threadLocalMap);
    }


    private void registerCleaner(final InternalThreadLocalMap threadLocalMap){
        Thread current = Thread.currentThread();
        /**检测当前索引是否存在清楚标志,存在则直接返回*/
        if(FastThreadLocalThread.willCleanupFastThreadLocals(current)||threadLocalMap.isCleanerFlagSet(index)){
            return;
        }
        threadLocalMap.setCleanerFlag(index);
    }

    private V initialize(InternalThreadLocalMap threadLocalMap){
        V v = null;
        try{
            v = initialValue();
        }catch (Exception e){
            PlatformDependent.throwException(e);
        }
        //在当前threadLocalMap对应索引位置添加相应的值
        threadLocalMap.setIndexedVariable(index, v);
        //将其添加到清除集合
        addToVariablesToRemove(threadLocalMap, this);
        return v;
    }

    protected V initialValue() throws Exception {
        return null;
    }


    /**检测当前FastLocal实例是否存在当前threaddLocal中*/
    public final boolean isSet() {
        return isSet(InternalThreadLocalMap.getIfSet());
    }

    /**检测当前FastLocal实例是否存在指定threaddLocal中*/
    public final boolean isSet(InternalThreadLocalMap threadLocalMap) {
        return threadLocalMap != null && threadLocalMap.isIndexedVariableSet(index);
    }


    /**
     * 如果传入的参数为UNSET表示将当前对象ThreadLocal删除
     * */
    public final void set(V value){
        if(value!=InternalThreadLocalMap.UNSET){
            //获取当前线程的ThreadLocalMap
            InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
        }else{
            remove();
        }
    }

    /**从当前线程的ThreadLocal移除当前FastThreadLocal实例*/
    public final void remove(){
        remove(InternalThreadLocalMap.getIfSet());
    }

    @SuppressWarnings("unchecked")
    public final void remove(InternalThreadLocalMap threadLocalMap){

        if(threadLocalMap==null){
            return;
        }
        //移除数组中对应的对象
        Object v = threadLocalMap.removeIndexedVariable(index);
        //从集合中移除
        removeFromVariablesToRemove(threadLocalMap,this);
        //证明原先存在值
        if(v!=InternalThreadLocalMap.UNSET){
            try{
                onRemoval((V) v);
            }catch (Exception e){
                PlatformDependent.throwException(e);
            }
        }
    }

    /**
     *  如果value为UNSET表示将当前threadLocalMap移除
     * */
    public final void set(InternalThreadLocalMap threadLocalMap,V value){
        if(value!=InternalThreadLocalMap.UNSET){
            setKnownNotUnset(threadLocalMap, value);
        }else {
            remove(threadLocalMap);
        }
    }

    /**
     * 将值添加寄到对应的Map
     * */
    private boolean setKnownNotUnset(InternalThreadLocalMap threadLocalMap, V value) {
        if (threadLocalMap.setIndexedVariable(index, value)) {
            addToVariablesToRemove(threadLocalMap, this);
            return true;
        }
        return false;
    }


    protected void onRemoval(@SuppressWarnings("UnusedParameters") V value) throws Exception { }




    /**获取可利用索引*/
    private static final int variablesToRemoveIndex = InternalThreadLocalMap.nextVariableIndex();


    /**移除当前线程的threadLocalMap的所有值
     * FastThreadLocalThread ==>  threadLocalMap   一一对应
     *
     * 在线程执行完毕调用FastThreadLocal.removeAll();

     * 1.获取当前线程的threadLocalMap

     * 2.获取FastThreadLocal集合 对threadLocalMap进行清空

     * 问题FastThreadLocal怎么存储threadLocalMap

     * set的时候找到当前线程的threadLocalMap,将对应的值存储进去

     * 为何这样设计?

     * 原来是因为可以创建多个FastThreadLocal,多个FastThreadLocal最终存储在variablesToRemoveArray集合里面


     * Thread 和 FastThreadLocal是多对多的关系 ,在这里遍历清除的只是所有FastThreadLocal里面同当前线程关联的属性值


     * 顺便值得一提,这样在线程运行完毕之后做这样的处理,可以防止内存泄露
     * */

    public static void removeAll() {

        /**获取线程对应的Map*/
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();

        if (threadLocalMap == null) {
            return;
        }

        try {
            //获取相应的FastThreadLocal集合,将其中与当前线程对应的threadLocalMap集合清空
            Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);

            if (v != null && v != InternalThreadLocalMap.UNSET) {

                @SuppressWarnings("unchecked")
                Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;

                FastThreadLocal<?>[] variablesToRemoveArray = variablesToRemove.toArray(new FastThreadLocal[0]);

                for (FastThreadLocal<?> tlv: variablesToRemoveArray) {
                    tlv.remove(threadLocalMap);
                }

            }
        } finally {
            InternalThreadLocalMap.remove();
        }
    }

    public static int size(){
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if(threadLocalMap==null){
            return 0;
        }else {
            return threadLocalMap.size();
        }
    }

    /**
     * 找到FastLocal类在Object对象的集合,将新的FastThreadLocal实例添加进去
     * */
    @SuppressWarnings("unchecked")
    private static void addToVariablesToRemove(InternalThreadLocalMap threadLocalMap,FastThreadLocal<?> variable){

        /**获取FastThreadLocal对应的对象*/
        Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);

        Set<FastThreadLocal<?>> variablesToRemove;

        if(v==InternalThreadLocalMap.UNSET||v==null){
            //将Map转化为Set
            variablesToRemove = Collections.newSetFromMap(new IdentityHashMap<FastThreadLocal<?>, Boolean>());
            threadLocalMap.setIndexedVariable(variablesToRemoveIndex,variablesToRemove);
        }else{
            variablesToRemove = (Set<FastThreadLocal<?>>) v;
        }
        variablesToRemove.add(variable);
    }


    private static void removeFromVariablesToRemove(InternalThreadLocalMap threadLocalMap, FastThreadLocal<?> variable) {

        Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);

        if (v == InternalThreadLocalMap.UNSET || v == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;

        variablesToRemove.remove(variable);
    }





}
