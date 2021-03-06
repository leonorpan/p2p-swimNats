package se.kth.swim.msg.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import se.kth.swim.msg.Ping;
import se.kth.swim.msg.Pong;
import se.sics.kompics.network.Header;
import se.sics.p2ptoolbox.util.network.NatedAddress;

public class NetPong extends NetMsg<Pong> {

	Pong elements;
	
	public NetPong(NatedAddress src, NatedAddress dst) {
		super(src, dst, new Pong());
	}



	public NetPong(NatedAddress src, NatedAddress dst,Pong elements) {
		super(src, dst, elements);
	}
	


	private NetPong(Header<NatedAddress> header, Pong content) {
		super(header, content);
	}
	@Override
	public NetMsg<Pong> copyMessage(Header<NatedAddress> newHeader) {
		// TODO Auto-generated method stub
		return new NetPong(newHeader, getContent());
	}

}
