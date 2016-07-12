/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package gm.com.dosya.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class TextDetailDocumentsCell extends FrameLayout {

    private TextView textView;
    private TextView valueTextView;
    private TextView typeTextView;
    private ImageView imageView;
    private CheckBox checkBox;

    public TextDetailDocumentsCell(Context context) {
        super(context);

        textView = new TextView(context);
        textView.setTextColor(0xff212121);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setGravity(Gravity.LEFT);
        addView(textView);
        LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.setMargins(71,10,16,0);
        layoutParams.gravity = Gravity.LEFT;
        setLayoutParams(layoutParams);
        textView.setLayoutParams(layoutParams);

        valueTextView = new TextView(context);
        valueTextView.setTextColor(0xff8a8a8a);
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        valueTextView.setLines(1);
        valueTextView.setMaxLines(1);
        valueTextView.setSingleLine(true);
        valueTextView.setGravity(Gravity.LEFT);
        addView(valueTextView);
        layoutParams = (LayoutParams) valueTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.setMargins(71,35,16,0);
        layoutParams.gravity = Gravity.LEFT;
        valueTextView.setLayoutParams(layoutParams);

        typeTextView = new TextView(context);
        typeTextView.setBackgroundColor(0xff757575);
        typeTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        typeTextView.setGravity(Gravity.CENTER);
        typeTextView.setSingleLine(true);
        typeTextView.setTextColor(0xffd1d1d1);
        typeTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        typeTextView.setTypeface(Typeface.DEFAULT_BOLD);
        addView(typeTextView);
        layoutParams = (LayoutParams) typeTextView.getLayoutParams();
        layoutParams.setMargins(16,40,0,40);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        typeTextView.setLayoutParams(layoutParams);

        imageView = new ImageView(context);
        addView(imageView);
        layoutParams = (LayoutParams) imageView.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(40);
        layoutParams.height = AndroidUtilities.dp(40);
        layoutParams.setMargins(16,0,0,0);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        imageView.setLayoutParams(layoutParams);

        checkBox = new CheckBox(context);
        checkBox.setVisibility(GONE);
        addView(checkBox);
        layoutParams = (LayoutParams) checkBox.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(22);
        layoutParams.height = AndroidUtilities.dp(22);
        layoutParams.topMargin = AndroidUtilities.dp(34);
        layoutParams.setMargins(38,0,0,0);
        layoutParams.gravity = Gravity.LEFT;
        checkBox.setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64), MeasureSpec.EXACTLY));
    }

    public void setTextAndValueAndTypeAndThumb(String text, String value, String type, String thumb, int resId,ContentResolver cr) {
        textView.setText(text);
        valueTextView.setText(value);
        if (type != null) {
            typeTextView.setVisibility(VISIBLE);
            typeTextView.setText(type);
        } else {
            typeTextView.setVisibility(GONE);
        }
        if (thumb != null || resId != 0) {
            if (thumb != null) {
                imageView.setImageBitmap(getThumbnail(cr,thumb));
            } else {
                imageView.setImageResource(resId);
            }
            imageView.setVisibility(VISIBLE);
        } else {
            imageView.setVisibility(GONE);
        }
    }

    public void setChecked(boolean checked, boolean animated) {
        if (checkBox.getVisibility() != VISIBLE) {
            checkBox.setVisibility(VISIBLE);
        }
        checkBox.setChecked(checked);
    }

    public static Bitmap getThumbnail(ContentResolver cr, String path) {

        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null );
        }

        ca.close();
        return null;

    }
}
