package se.kth.swim.msg.net;

import se.kth.swim.msg.FinalPong;
import se.kth.swim.msg.Pong;
import se.sics.kompics.network.Header;
import se.sics.p2ptoolbox.util.network.NatedAddress;


public class NetFinalPong extends NetMsg<FinalPong>{

	public NetFinalPong(NatedAddress src, NatedAddress dst, FinalPong content) {
		super(src, dst,content);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NetMsg copyMessage(Header<NatedAddress> newHeader) {
		// TODO Auto-generated method stub
		return new NetFinalPong(newHeader,getContent());
	}

	public NetFinalPong(Header<NatedAddress> header, FinalPong content) {
		super(header, content);
		// TODO Auto-generated constructor stub
	}
}
