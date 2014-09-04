package net.oschina.app.v2.emoji;

import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;

import net.oschina.app.AppContext;
import net.oschina.app.R;

public class EmojiHelper {

	public static Map<String, Emoji> qq_emojis = new HashMap<String, Emoji>();

	public final static String EMOJI_PREFIX = "[";
	public final static String EMOJI_SUFFIX = "]";

	static {
		// Emoji emoji = new Emoji(R.drawable.f001, "[微笑]");
		// emojis.put(emoji.getValue(), emoji);
		// emoji = new Emoji(R.drawable.f002, "[撇嘴]");
		// emojis.put(emoji.getValue(), emoji);
		// emoji = new Emoji(R.drawable.f003, "[色]");
		// emojis.put(emoji.getValue(), emoji);
	}

	public static Emoji getEmoji(String val) {
		return qq_emojis.get(val.substring(1, val.length() - 1));
	}

	public static void initEmojis() {
		AppContext contenxt = AppContext.instance();
		Resources res = contenxt.getResources();
		String[] vals = res.getStringArray(R.array.qq_emoji_vals);
		for (int i = 0; i < 104; i++) {
			int id = res.getIdentifier("smiley_" + i, "drawable",
					contenxt.getPackageName());
			Emoji emoji = new Emoji(id, vals[i]);
			qq_emojis.put(vals[i], emoji);
		}
	}
}
