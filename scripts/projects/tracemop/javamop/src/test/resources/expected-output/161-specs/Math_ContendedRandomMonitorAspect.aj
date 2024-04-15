package mop;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

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

public aspect Math_ContendedRandomMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Math_ContendedRandomMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Math_ContendedRandom_MOPLock = new ReentrantLock();
	static Condition Math_ContendedRandom_MOPLock_cond = Math_ContendedRandom_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() && BaseAspect.notwithin();
	pointcut Math_ContendedRandom_onethread_use() : (call(* Math.random(..))) && MOP_CommonPointCut();
	before () : Math_ContendedRandom_onethread_use() {
		Thread t = Thread.currentThread();
		//Math_ContendedRandom_otherthread_use
		Math_ContendedRandomRuntimeMonitor.otherthread_useEvent(t);
		//Math_ContendedRandom_onethread_use
		Math_ContendedRandomRuntimeMonitor.onethread_useEvent(t);
	}

}
