package com.doge.dyjw.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.doge.dyjw.MainActivity;
import com.doge.dyjw.R;
import com.doge.dyjw.util.Log;

import java.io.File;

/**
 * Created by 政 on 2015/7/8.
 */
public class UrlTextView extends TextView {
    OnDownloadListener listener;

    public UrlTextView(Context context) {
        super(context);
    }

    public void setText(Spanned text, OnDownloadListener listener) {
        super.setText(text);
        Log.v("URLSpan", text.toString());
        this.listener = listener;
        CharSequence cText = getText();
        int end = cText.length();
        Spanned sp = (Spanned) getText();
        URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
        SpannableStringBuilder style = new SpannableStringBuilder(cText);
        style.clearSpans();
        for(URLSpan url : urls){
            MyURLSpan myURLSpan = new MyURLSpan(url.getURL(), style.subSequence(sp.getSpanStart(url), sp.getSpanEnd(url)).toString());
            Log.v("URLSpan", sp.getSpanStart(url) + "," + sp.getSpanEnd(url) + "-----" + style.subSequence(sp.getSpanStart(url), sp.getSpanEnd(url)));
            style.setSpan(myURLSpan,sp.getSpanStart(url),
                    sp.getSpanEnd(url),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        super.setText(style);
    }

    class MyURLSpan extends ClickableSpan {
        private String url;
        private String text;
        private static final String types = ".doc.docx.xls.xlsx.ppt.pptx.pdf.rar.zip.7z"
                + ".apk.avi.mp3.mp4.flv";
        MyURLSpan(String url, String text) {
            if(!text.contains(".") ||
                    !url.substring(url.lastIndexOf(".")).toLowerCase()
                    .equals(text.substring(text.lastIndexOf(".")).toLowerCase())) {
                text += url.substring(url.lastIndexOf("."));
            }
            this.url =url;
            this.text = text;
        }
        @Override
        public void onClick(View widget) {
            if(url.contains(".") && types.contains(url.substring(url.lastIndexOf(".")).toLowerCase())) {
                showDialog(url, text);
            } else {
                openOuter(url);
            }
        }
    }

    private void openOuter(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        getContext().startActivity(intent);
    }

    private void showDialog(final String url, final String text) {
        View view = inflate(getContext(), R.layout.alert_download, null);
        final EditText filename = (EditText) view.findViewById(R.id.filename);
        filename.setText(text.substring(0, text.lastIndexOf(".")));
        final String filetype = text.substring(text.lastIndexOf("."));
        if(new File(MainActivity.DOWNLOAD_DIR + "/" + text).exists()
                || new File(MainActivity.DOWNLOAD_DIR + "/" + text + ".tmp").exists()) {
            for(int i = 1; ; i++) {
                if(!new File(MainActivity.DOWNLOAD_DIR + "/" +
                        text.replace(filetype, "(" + i + ")" + filetype)).exists()
                        && !new File(MainActivity.DOWNLOAD_DIR + "/" +
                        text.replace(filetype, "(" + i + ")" + filetype + ".tmp")).exists()) {
                    String filenameStr = text.replace(filetype, "(" + i + ")" + filetype);
                    filename.setText(filenameStr.substring(0, filenameStr.lastIndexOf(".")));
                    break;
                }
            }
        }
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.download_file)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener.download(url, filename.getText().toString()
                                + filetype)) {
                            showDialog(url, text);
                        }
                    }
                })
                .setNeutralButton(R.string.open_outer, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openOuter(url);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public interface OnDownloadListener {
        public boolean download(String url, String filename);
    }
}
