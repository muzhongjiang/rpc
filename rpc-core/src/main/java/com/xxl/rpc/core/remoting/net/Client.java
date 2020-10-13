package com.xxl.rpc.core.remoting.net;

import com.xxl.rpc.core.remoting.invoker.reference.RpcReferenceBean;
import com.xxl.rpc.core.remoting.net.params.RpcRequest;

/**
 * i client
 * @author mzj 2015-11-24 22:18:10
 */
public abstract class Client {

	// ---------------------- init ----------------------

	protected volatile RpcReferenceBean xxlRpcReferenceBean;

	public void init(RpcReferenceBean xxlRpcReferenceBean) {
		this.xxlRpcReferenceBean = xxlRpcReferenceBean;
	}


    // ---------------------- send ----------------------

	/**
	 * async send, bind requestId and future-response
	 *
	 * @param address
	 * @param xxlRpcRequest
	 * @return
	 * @throws Exception
	 */
	public abstract void asyncSend(String address, RpcRequest xxlRpcRequest) throws Exception;

}
