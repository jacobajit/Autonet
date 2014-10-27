package com.el1t.iolite;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by El1t on 10/21/14.
 */
public class EighthActivityXmlParser
{
	private static final String TAG = "Activity List XML Parser";

	public static boolean parseSuccess(InputStream in) throws XmlPullParserException, IOException {
		// Initialize parser and jump to first tag
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readResponse(parser);
		} finally {
			in.close();
		}
	}

	private static boolean readResponse(XmlPullParser parser) throws XmlPullParserException, IOException {
		boolean response = false;
		parser.require(XmlPullParser.START_TAG, null, "eighth");
		// Consume the eighth AND signup tags
		parser.next();
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Look for success tag
			if (name.equals("signup")) {
				while (parser.getEventType() != XmlPullParser.END_TAG) {
					if (parser.getEventType() != XmlPullParser.START_TAG) {
						continue;
					}
					if (parser.getName().equals("success")) {
						if (parser.next() == XmlPullParser.TEXT) {
							response = parser.getText().equals("1");
						}
					}
				}
			} else if (name.equals("error")) {
				if (parser.next() == XmlPullParser.TEXT) {
					Log.e(TAG, parser.getText());
				}
			} else {
				skip(parser);
			}
		}
		return response;
	}

// ============ Parse activity list =============

	public static ArrayList<EighthActivityItem> parse(InputStream in) throws XmlPullParserException, IOException {
		// Initialize parser and jump to first tag
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readEighth(parser);
		} finally {
			in.close();
		}
	}

	private static ArrayList<EighthActivityItem> readEighth(XmlPullParser parser) throws XmlPullParserException, IOException {
		ArrayList<EighthActivityItem> entries = new ArrayList<EighthActivityItem>();
		parser.require(XmlPullParser.START_TAG, null, "eighth");
		// Consume the eighth AND activities tags
		parser.next();
		while(parser.next() != XmlPullParser.START_TAG) {
			parser.next();
		}
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the activity tag
			if (name.equals("activity")) {
				entries.add(readActivity(parser));
			} else {
				skip(parser);
			}
		}
		return entries;
	}

	public static EighthActivityItem readActivity(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, "activity");

		int AID = 0;
		String name = null;
		String description = null;
		boolean restricted = false;
		boolean presign = false;
		boolean oneaday = false;
		boolean bothblocks = false;
		boolean sticky = false;
		boolean special = false;
		boolean calendar = false;
		boolean roomChanged = false;
		ArrayList<Integer> blockSponsors = null;
		ArrayList<Integer> blockRooms = null;
		String blockRoomString = null;
		int BID = 0;
		boolean cancelled = false;
		String comment = null;
		String advertisement = null;
		boolean attendanceTaken = false;
		boolean favorite = false;
		int memberCount = 0;
		int capacity = 0;

		while (parser.next() != XmlPullParser.END_TAG) {
			// Skip whitespace until a tag is reached
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String tagName = parser.getName();

			if (tagName.equals("aid")) {
				AID = readInt(parser, "aid");
			} else if (tagName.equals("name")) {
				name = readString(parser, "name");
			} else if (tagName.equals("description")) {
				description = readString(parser, "description");
			} else if (tagName.equals("restricted")) {
				restricted = readBool(parser, "restricted");
			} else if (tagName.equals("presign")) {
				presign = readBool(parser, "presign");
			} else if (tagName.equals("oneaday")) {
				oneaday = readBool(parser, "oneaday");
			} else if (tagName.equals("bothblocks")) {
				bothblocks = readBool(parser, "bothblocks");
			} else if (tagName.equals("sticky")) {
				sticky = readBool(parser, "sticky");
			} else if (tagName.equals("special")) {
				special = readBool(parser, "special");
			} else if (tagName.equals("calendar")) {
				calendar = readBool(parser, "calendar");
			} else if (tagName.equals("room_changed")) {
				roomChanged = readBool(parser, "room_changed");
			} else if (tagName.equals("block_sponsor")) {
				blockSponsors = readNestedInts(parser, "block_sponsor");
			} else if (tagName.equals("block_room")) {
				blockRooms = readNestedInts(parser, "block_room");
			} else if (tagName.equals("block_rooms_comma")) {
				blockRoomString = readString(parser, "block_rooms_comma");
			} else if (tagName.equals("bid")) {
				BID = readInt(parser, "bid");
			} else if (tagName.equals("cancelled")) {
				cancelled = readBool(parser, "cancelled");
			} else if (tagName.equals("comment")) {
				comment = readString(parser, "comment");
			} else if (tagName.equals("advertisement")) {
				advertisement = readString(parser, "advertisement");
			} else if (tagName.equals("attendancetaken")) {
				attendanceTaken = readBool(parser, "attendancetaken");
			} else if (tagName.equals("favorite")) {
				favorite = readBool(parser, "favorite");
			} else if (tagName.equals("member_count")) {
				memberCount = readInt(parser, "member_count");
			} else if (tagName.equals("capacity")) {
				capacity = readInt(parser, "capacity");
			} else {
				skip(parser);
			}
		}
		if (AID * BID == 0) {
			Log.e(TAG, "Malformed integer in fields for activity " + name);
		}
		parser.require(XmlPullParser.END_TAG, null, "activity");

		return new EighthActivityItem(AID, name, description, restricted, presign, oneaday,
				bothblocks, sticky, special, calendar, roomChanged, blockSponsors,
				blockRooms, blockRoomString, BID, cancelled, comment,
				advertisement, attendanceTaken, favorite, memberCount,
				capacity);
	}

	protected static String readString(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, tagName);
		// Note: this cannot be null, because some fields are empty! (empty fields would have to be set to "", anyways)
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, null, tagName);
		return result;
	}

	protected static int readInt(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, tagName);
		int result = 0;
		if (parser.next() == XmlPullParser.TEXT) {
			result = Integer.parseInt(parser.getText());
			parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, null, tagName);
		return result;
	}

	protected static boolean readBool(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, tagName);
		boolean result = false;
		if (parser.next() == XmlPullParser.TEXT) {
			result = Integer.parseInt(parser.getText()) == 1;
			parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, null, tagName);
		return result;
	}

	// Read integers inside another tag
	protected static ArrayList<Integer> readNestedInts(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
		ArrayList<Integer> result = new ArrayList<Integer>();
		parser.require(XmlPullParser.START_TAG, null, tagName);
		while(parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			// Eliminates the plural 's' (e.g. sponsors --> sponsor)
			if (parser.getName().equals(tagName.substring(0, tagName.length() - 1))) {
				if (parser.next() == XmlPullParser.TEXT) {
					result.add(Integer.parseInt(parser.getText()));

					parser.nextTag();
				}
			}
		}
		parser.require(XmlPullParser.END_TAG, null, tagName);
		return result;
	}

	protected static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}
}