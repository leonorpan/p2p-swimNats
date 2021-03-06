package se.kth.swim.msg.net;

import se.kth.swim.msg.IndirectPong;
import se.sics.kompics.network.Header;
import se.sics.p2ptoolbox.util.network.NatedAddress;

public class NetIndirectPong extends NetMsg<IndirectPong> {

	public NetIndirectPong(NatedAddress src, NatedAddress dst, IndirectPong content) {
		super(src, dst, content);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NetMsg copyMessage(Header<NatedAddress> newHeader) {
		// TODO Auto-generated method stub
		return new NetIndirectPong(newHeader, getContent());
	}

	public NetIndirectPong(Header<NatedAddress> header, IndirectPong content) {
		super(header, content);
		// TODO Auto-generated constructor stub
	}
}
