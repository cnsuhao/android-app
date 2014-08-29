package net.oschina.app.v2.ui.pagertab;

import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import java.util.*;

import net.oschina.app.v2.utils.aa;

// Referenced classes of package im.yixin.application:
//            s, q, c

public final class r {

	public r() {
		// a(a);
	}

	static final void a(SparseArray sparsearray) {
		sparsearray.put(0, new q(0));
		q q1 = new q(1);
		sparsearray.put(1, q1);
		q q2 = new q(4);
		q q3 = new q(5);
		q q4 = new q(11);
		q q5 = new q(7);
		q q6 = new q(8);
		q q7 = new q(10);
		q q8 = new q(12);
		q q9 = new q(13);
		q q10 = new q(14);
		sparsearray.put(4, q2);
		sparsearray.put(5, q3);
		sparsearray.put(11, q4);
		sparsearray.put(12, q8);
		sparsearray.put(7, q5);
		sparsearray.put(8, q6);
		sparsearray.put(10, q7);
		sparsearray.put(13, q9);
		sparsearray.put(14, q10);
		q1.a(q2);
		q1.a(q3);
		q1.a(q4);
		q1.a(q8);
		q1.a(q10);
		q4.a(q5);
		sparsearray.put(2, new q(2));
		q q11 = new q(3);
		sparsearray.put(3, q11);
		q11.a(q9);
		q q12 = new q(6);
		sparsearray.put(6, q12);
		q11.a(q12);
	}

	public static void a(q q1, TextView textview, View view) {
		int i = 0;
		if (q1 != null && textview != null && view != null) {
			int j = q1.b();
			boolean flag = q1.c();
			int l;
			if (j > 0)
				l = 0;
			else
				l = 8;
			textview.setVisibility(l);
			if (!flag)
				i = 8;
			view.setVisibility(i);
			if (j > 0)
				textview.setText(String.valueOf(Math.min(99, j)));
		} else {
			aa.a(textview, 8);
			aa.a(view, 8);
		}// goto _L2; else goto _L1
	}

	// public final q a(int i)
	// {
	// return new q((q) a.get(i));
	// }
	//
	// public final List a()
	// {
	// SparseArray sparsearray = a;
	// ArrayList arraylist = new ArrayList();
	// for (int i = 0; i < sparsearray.size(); i++)
	// arraylist.add(new q((q) sparsearray.valueAt(i)));
	//
	// return arraylist;
	// }
	//
	// final void a(boolean flag)
	// {
	// ((q) a.get(2)).a(im.yixin.application.c.x().d());
	// if (flag)
	// {
	// int ai[] = new int[1];
	// ai[0] = 2;
	// a(ai);
	// }
	// }
	//
	// final transient void a(int ai[])
	// {
	// HashSet hashset = new HashSet();
	// int i = ai.length;
	// for (int j = 0; j < i; j++)
	// {
	// int i1 = ai[j];
	// q q2 = (q) a.get(i1);
	// Remote remote = new Remote();
	// remote.a(1);
	// remote.b(20);
	// remote.a(new q(q2));
	// h.a().e(remote);
	// if (q2.e() >= 0)
	// hashset.add(Integer.valueOf(q2.e()));
	// }
	//
	// int ai1[];
	// for (Iterator iterator = hashset.iterator(); iterator.hasNext(); a(ai1))
	// {
	// int l = ((Integer) iterator.next()).intValue();
	// q q1 = (q) a.get(l);
	// if (q1 != null)
	// q1.d();
	// ai1 = new int[1];
	// ai1[0] = l;
	// }
	//
	// }
	//
	// public final void b()
	// {
	// if (!b)
	// {
	// b = true;
	// a(false);
	// b(false);
	// d(k.d());
	// e(im.yixin.plugin.game.a.a());
	// c.a(true);
	// }
	// }
	//
	// final void b(boolean flag)
	// {
	// c c1 = im.yixin.plugin.sns.c.a();
	// if(c1 != null) goto _L2; else goto _L1
	// _L1:
	// return;
	// _L2:
	// q q1 = (q)a.get(4);
	// q1.a(c1.b((byte)2));
	// boolean flag1;
	// if(c1.b((byte)1) > 0)
	// flag1 = true;
	// else
	// flag1 = false;
	// q1.a(flag1);
	// if(flag)
	// {
	// int ai[] = new int[1];
	// ai[0] = 4;
	// a(ai);
	// }
	// if(true) goto _L1; else goto _L3
	// _L3:
	// }
	//
	// public final void c(boolean flag)
	// {
	// ((q) a.get(11)).a(flag);
	// int ai[] = new int[1];
	// ai[0] = 11;
	// a(ai);
	// }
	//
	// final void d(boolean flag)
	// {
	// ((q) a.get(6)).a(flag);
	// int ai[] = new int[1];
	// ai[0] = 6;
	// a(ai);
	// }
	//
	// final void e(boolean flag)
	// {
	// ((q) a.get(12)).a(flag);
	// int ai[] = new int[1];
	// ai[0] = 12;
	// a(ai);
	// }
	//
	// final SparseArray a = new SparseArray();
	// boolean b;
	// final g c = new s(this);
}
