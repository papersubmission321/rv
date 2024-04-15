package mop;
import java.util.*;
import java.lang.reflect.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

aspect BaseAspect {
	pointcut notwithin() :
	!within(sun..*) &&
	!within(java..*) &&
	!within(javax..*) &&
	!within(com.sun..*) &&
	!within(org.dacapo.harness..*) &&
	!within(org.apache.commons..*) &&
	!within(org.apache.geronimo..*) &&
	!within(net.sf.cglib..*) &&
	!within(mop..*) &&
	!within(javamoprt..*) &&
	!within(rvmonitorrt..*) &&
	!within(com.runtimeverification..*);
}

public aspect Map_ItselfAsKeyMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Map_ItselfAsKeyMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Map_ItselfAsKey_MOPLock = new ReentrantLock();
	static Condition Map_ItselfAsKey_MOPLock_cond = Map_ItselfAsKey_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() && BaseAspect.notwithin();
	pointcut Map_ItselfAsKey_putall(Map map, Map src) : (call(* Map+.putAll(Map)) && target(map) && args(src)) && MOP_CommonPointCut();
	before (Map map, Map src) : Map_ItselfAsKey_putall(map, src) {
		Map_ItselfAsKeyRuntimeMonitor.putallEvent(map, src);
	}

	pointcut Map_ItselfAsKey_put(Map map, Object key, Object value) : (call(* Map+.put(Object, Object)) && target(map) && args(key, value)) && MOP_CommonPointCut();
	before (Map map, Object key, Object value) : Map_ItselfAsKey_put(map, key, value) {
		Map_ItselfAsKeyRuntimeMonitor.putEvent(map, key, value);
	}

}
