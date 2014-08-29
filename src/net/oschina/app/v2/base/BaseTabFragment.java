package net.oschina.app.v2.base;


public class BaseTabFragment extends BaseFragment {
	public static interface a {

		public abstract boolean isCurrent(BaseTabFragment yixintabfragment);
	}

	public BaseTabFragment() {
	}

	public final void a(a a1) {
		e = a1;
	}

	protected final boolean e() {
		return e.isCurrent(this);
	}

	public void f() {
	}

	public void g() {
	}

	public void h() {
	}

	public void i() {
	}

	private a e;
}
