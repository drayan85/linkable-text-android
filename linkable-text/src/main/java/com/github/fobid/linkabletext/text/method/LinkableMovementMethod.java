package com.github.fobid.linkabletext.text.method;

import android.text.Layout;
import android.text.Selection;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Patterns;
import android.view.MotionEvent;

import com.github.fobid.linkabletext.annotation.LinkType;
import com.github.fobid.linkabletext.view.LinkableCallback;
import com.github.fobid.linkabletext.widget.LinkableTextView;

/**
 * Created by partner on 2016-11-17.
 */

public class LinkableMovementMethod extends LinkMovementMethod {

    private static final String LINKABLE_BASE_SCHEME = "https://github.com/fobid/linkable-text-android";
    public static final String LINKABLE_HASHTAG_SCHEME = LINKABLE_BASE_SCHEME + "/hashtag";
    public static final String LINKABLE_MENTION_SCHEME = LINKABLE_BASE_SCHEME + "/mention";
    public static final String LINKABLE_IP_ADDRESS_SCHEME = LINKABLE_BASE_SCHEME + "/ip";

    private final LinkableCallback mLinkableCallback;

    public LinkableMovementMethod(LinkableCallback linkableCallback) {
        super();
        if (linkableCallback == null) {
            mLinkableCallback = new LinkableCallback() {
                @Override
                public void onMatch(@LinkType int type, String value) {
                }
            };
        } else {
            mLinkableCallback = linkableCallback;
        }
    }

    public boolean onTouchEvent(android.widget.TextView widget, android.text.Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if (link.length != 0) {
                String url = link[0].getURL();
                handleLink(url);

                // Remove selected background
                Selection.removeSelection(buffer);
                return true;
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    private void handleLink(String link) {
        if (link.startsWith(LINKABLE_HASHTAG_SCHEME)) {
            String hashtag = link.replaceFirst(LINKABLE_HASHTAG_SCHEME, "");
            hashtag = hashtag.replaceFirst(".*#", "");
            mLinkableCallback.onMatch(LinkableTextView.Link.HASH_TAG, hashtag);
        } else if (link.startsWith(LINKABLE_MENTION_SCHEME)) {
            String mention = link.replaceFirst(LINKABLE_MENTION_SCHEME, "");
            mention = mention.replaceFirst(".*@", "");
            mLinkableCallback.onMatch(LinkableTextView.Link.MENTION, mention);
        } else if (link.startsWith(LINKABLE_IP_ADDRESS_SCHEME)) {
            String ip = link.replaceFirst(LINKABLE_IP_ADDRESS_SCHEME, "");
            ip = ip.replaceFirst(".", "");
            mLinkableCallback.onMatch(LinkableTextView.Link.IP_ADDRESS, ip);
        } else if (Patterns.EMAIL_ADDRESS.matcher(link).matches()) {
            mLinkableCallback.onMatch(LinkableTextView.Link.EMAIL_ADDRESS, link);
        } else if (Patterns.IP_ADDRESS.matcher(link).matches()
                || Patterns.DOMAIN_NAME.matcher(link).matches()
                || Patterns.WEB_URL.matcher(link).matches()) {
            mLinkableCallback.onMatch(LinkableTextView.Link.WEB_URL, link);
        } else if (Patterns.PHONE.matcher(link).matches()) {
            mLinkableCallback.onMatch(LinkableTextView.Link.PHONE, link);
        }
    }
}
