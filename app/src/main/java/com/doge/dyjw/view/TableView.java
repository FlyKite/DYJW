package com.doge.dyjw.view;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.HorizontalScrollView;

import com.doge.dyjw.R;
import com.doge.dyjw.util.Log;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 政 on 2015/7/2.
 */
public class TableView extends HorizontalScrollView {
    private Context context;
    private UrlTextView.OnDownloadListener download;

    public TableView(Context context, Element table, UrlTextView.OnDownloadListener download) {
        super(context);
        Log.v("TableView", "newTableView");
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.context = context;
        this.download = download;
        init(table);
    }

    private void init(Element table) {
        Log.v("TableView", "initTableView");
        Elements rows = table.select("tr");
        GridLayout layout = new GridLayout(getContext());
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout.setBackgroundColor(context.getResources().getColor(R.color.line_gray));
        List<Map<String, Integer>> spanList = new ArrayList<>();
        int rowNum = 0;
        for(Element row : rows) {
            int colNum = 0;
            for(Element col : row.select("td")) {
                UrlTextView t = new UrlTextView(layout.getContext());
                int rowSpan = 1;
                int colSpan = 1;
                try {
                    colSpan = Integer.parseInt(col.attr("colspan"));
                } catch (Exception ignored) { }
                try {
                    rowSpan = Integer.parseInt(col.attr("rowspan"));
                    HashMap<String, Integer> span = new HashMap<>();
                    span.put("row", rowNum);
                    span.put("col", colNum);
                    span.put("rowSpan", rowSpan);
                    span.put("colSpan", colSpan);
                    spanList.add(span);
                } catch (Exception ignored) { }
                for(Map<String, Integer> span : spanList) {
                    if(rowNum == span.get("row")) continue;
                    if(colNum == span.get("col")) {
                        colNum += span.get("colSpan");
                    }
                }
                GridLayout.Spec rowSpec = GridLayout.spec(rowNum, rowSpan);
                GridLayout.Spec colSpec = GridLayout.spec(colNum, colSpan);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
                params.setGravity(Gravity.FILL);
                if(rowNum == 0) params.width = 3;
                params.setMargins(1, 1, 1, 1);
                t.setLayoutParams(params);
                t.setPadding(10, 0, 10, 0);
                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                t.setBackgroundColor(context.getResources().getColor(R.color.window_background_light));
                t.setText(Html.fromHtml(col.toString()), download);
                t.setTextColor(getResources().getColor(R.color.text_black));
                t.setMovementMethod(LinkMovementMethod.getInstance());
                layout.addView(t);
                colNum += colSpan;
            }
            rowNum++;
            for(int i = 0; i < spanList.size(); i++) {
                Map<String, Integer> span = spanList.get(i);
                if(span.get("row") + span.get("rowSpan") == rowNum) {
                    spanList.remove(i--);
                }
            }
        }
        addView(layout);
    }

//    int row = 0;
//    int rowSpan = 0;
//    int colSpan = 0;
//    int lastRow = 0;
//    int maxCols = 0;
//    int colCount = 0;
//    int windowWidth = 获取屏幕宽度;
//    int colWidth = 0;
//    int index = 0;
//    View view = null;
//    List<Integer> filledCellList = new ArrayList<Integer>();
//    do {
//        row = ...;
//        rowSpan = ...;
//        colSpan = ...;
//        maxCols = ...;
//        GridLayout gridLayout = new GridLayout(context);
//        gridLayout.setColumnCount(newMaxCols);
//        gridLayout.setUseDefaultMargins(true);
//        gridLayout.setOrientation(GridLayout.HORIZONTAL);
//        gridLayout.setBackgroundResource(R.color.dark_green);
//        gridLayout.setAlignmentMode(GridLayout.ALIGN_MARGINS);
//        colWidth = windowWidth / maxCols;
//        if (row > lastRow) {
//            lastRow = row;
//            colCount = 1;
//        }
//        view = createView(...);
//        if (view != null) {
//            view.setBackgroundColor(Color.WHITE);
//            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//            params.setMargins(1, 1, 1, 1);
//            params.width = colWidth * colSpan;
//            index = maxCols * (row - 1) + colCount - 1;
//            while (filledCellList.contains(index)) {
//                colCount++;
//                index = maxCols * (row - 1) + colCount - 1;
//            }
//            if (rowSpan > 1) {
//                for (int j = row; j < row + rowSpan; j++) {
//                    for (int i = colCount; i < colCount + colSpan; i++) {
//                        if (j == row && i == colCount) {
//                            continue;
//                        }
//                        index = maxCols * (j - 1) + i - 1; filledCellList.add(index);
//                    }
//                }
//            }
//            params.columnSpec = GridLayout.spec(colCount - 1, colSpan, GridLayout.FILL);
//            params.rowSpec = GridLayout.spec(row - 1, rowSpan, GridLayout.FILL);
//            gridLayout.addView(view, params); colCount += colSpan;
//        }
//    } while (cursor.moveToNext()); //一个Cursor数据代表一个单元格数据 最终将GridLayout加入到ScrollView,实现滑动显示.
}
