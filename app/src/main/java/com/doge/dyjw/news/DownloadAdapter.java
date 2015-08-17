package com.doge.dyjw.news;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doge.dyjw.MainActivity;
import com.doge.dyjw.R;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by 政 on 2015/7/25.
 */
public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {
    private List<Map<String, Object>> list;
    private DownloadService service;
    public DownloadAdapter(List<Map<String, Object>> list, DownloadService service) {
        this.list = list;
        this.service = service;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_downloaded_file, viewGroup, false);
        // set the view's size, margins, paddings and layout parameters
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Map<String, Object> map = list.get(i);
        String filename = map.get("filename").toString();
        viewHolder.filename.setText(filename);
        String status = map.get("status").toString();
        viewHolder.delete.setOnClickListener(new DeleteListener(filename));
        if (status.equals("finished")) {
            viewHolder.start_stop.setVisibility(View.GONE);
            viewHolder.speed.setVisibility(View.GONE);
            viewHolder.file_card.setOnClickListener(new OpenListener(filename));
        } else if (status.equals("stoped")) {
            viewHolder.start_stop.setImageResource(R.drawable.start);
            viewHolder.start_stop.setOnClickListener(new DownloadListener(filename).setDownloading(false));
            viewHolder.speed.setText("正在发呆");
        } else if(status.equals("error")) {
            viewHolder.start_stop.setImageResource(R.drawable.start);
            viewHolder.start_stop.setOnClickListener(new DownloadListener(filename).setDownloading(true));
            Object obj = map.get("speed");
            if(obj != null) {
                viewHolder.speed.setText("error:" + obj.toString());
            } else {
                viewHolder.speed.setText("error:");
            }
        } else {
            viewHolder.start_stop.setImageResource(R.drawable.stop);
            viewHolder.start_stop.setOnClickListener(new DownloadListener(filename).setDownloading(true));
            Object obj = map.get("speed");
            if(obj != null) {
                viewHolder.speed.setText(obj.toString());
            } else {
                viewHolder.speed.setText("");
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton start_stop;
        public ImageButton delete;
        public TextView filename;
        public TextView speed;
        public RelativeLayout file_card;
        public ViewHolder(View itemView) {
            super(itemView);
            filename = (TextView) itemView.findViewById(R.id.filename);
            speed = (TextView) itemView.findViewById(R.id.speed);
            start_stop = (ImageButton) itemView.findViewById(R.id.download_status);
            delete = (ImageButton) itemView.findViewById(R.id.delete);
            file_card = (RelativeLayout) itemView.findViewById(R.id.file_card);
        }
    }

    class DownloadListener implements View.OnClickListener {
        String filename;
        public DownloadListener(String filename) {
            this.filename = filename;
        }
        boolean downloading;
        public DownloadListener setDownloading(boolean downloading) {
            this.downloading = downloading;
            return this;
        }
        @Override
        public void onClick(View v) {
            if(downloading) {
                service.stopDownload(filename);
            } else {
                service.continueDownload(filename);
            }
            downloading = !downloading;
        }
    }

    class OpenListener implements View.OnClickListener {
        String filename;
        public OpenListener(String filename) {
            this.filename = filename;
        }
        @Override
        public void onClick(View v) {
            File file = new File(MainActivity.DOWNLOAD_DIR + "/" + filename);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.intent.action.VIEW");
            intent.setDataAndType(Uri.fromFile(file), getMIMEType(file, v.getContext()));
            v.getContext().startActivity(intent);
        }
    }

    public String getMIMEType(File file, Context context) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0) {
            return "*/*";
        }
        String filetype = fileName.substring(dotIndex, fileName.length());
        String[] filetypes = context.getResources().getStringArray(R.array.filetype);
        String[] mimetypes = context.getResources().getStringArray(R.array.mimetype);
        for(int i = 0; i < filetypes.length; i++) {
            if(filetypes[i].equals(filetype.toLowerCase())) {
                return mimetypes[i];
            }
        }
        return "*/*";
    }

    class DeleteListener implements View.OnClickListener {
        String filename;
        public DeleteListener(String filename) {
            this.filename = filename;
        }
        @Override
        public void onClick(final View v) {
            new AlertDialog.Builder(v.getContext())
                    .setMessage(v.getContext().getString(R.string.delete_confirm) + filename)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            service.stopDownload(filename);
                            new File(MainActivity.DOWNLOAD_DIR + "/" + filename).delete();
                            new File(MainActivity.DOWNLOAD_DIR + "/" + filename + ".tmp").delete();
                            new File(MainActivity.DOWNLOAD_DIR + "/" + filename + ".tfp").delete();
                            for(Map<String, Object> map : list) {
                                if(map.get("filename").equals(filename)) {
                                    list.remove(map);
                                    Toast.makeText(v.getContext(), R.string.delete_finished, Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            notifyDataSetChanged();
                        }
                    })
                    .setNeutralButton(R.string.no, null)
                    .show();
        }
    }
}
