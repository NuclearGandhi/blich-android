package com.blackcracks.blich.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.ExamsEntry;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExamAdapter extends CursorRecyclerViewAdapter<ExamAdapter.ViewHolder> {

    private final static String LOG_TAG = ExamAdapter.class.getSimpleName();

    private Context mContext;

    public ExamAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(mContext).inflate(R.layout.exam_item, parent, false);


        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

        String subject = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_SUBJECT));
        String date = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_DATE));
        String teachers = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_TEACHER));

        Locale locale = Locale.getDefault();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", locale);
        Date examDate = null;
        try {
            examDate = dateFormat.parse(date);
        } catch (ParseException e) {
            Log.d(LOG_TAG, e.getMessage(), e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(examDate.getTime());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
        int year = calendar.get(Calendar.YEAR);

        viewHolder.examDay.setText(String.valueOf(day));
        viewHolder.examMonthYear.setText(month + ", " + year);
        viewHolder.examSubject.setText(subject);
        viewHolder.examTeachers.setText(teachers);

        int backgroundId = 0;
        boolean isBackground = true;
        if (subject.contains("מתמטיקה")) {
            backgroundId = R.drawable.subject_math;
        } else if (subject.contains("אנגלית")) {
            backgroundId = R.drawable.subject_english;
        } else if (subject.contains("פיזיקה")) {
            backgroundId = R.drawable.subject_physics;
        } else {
            isBackground = false;
            viewHolder.examBackground.setVisibility(View.GONE);
        }

        if (isBackground) {
            Picasso.with(mContext)
                    .load(backgroundId)
                    .transform(new Transformation() {
                        @Override
                        public Bitmap transform(Bitmap source) {

                            int x = source.getWidth();
                            int y = source.getHeight();

                            Bitmap gradientBitmap = source.copy(source.getConfig(), true);
                            Canvas canvas = new Canvas(gradientBitmap);
                            LinearGradient gradient = new LinearGradient(0, 0 , x, y,
                                    Color.WHITE, Color.WHITE,
                                    Shader.TileMode.CLAMP);

                            Paint paint = new Paint();
                            paint.setShader(gradient);
                            canvas.drawPaint(paint);
                            source.recycle();
                            return gradientBitmap;
                        }

                        @Override
                        public String key() {
                            return "brighten";
                        }
                    })
                    .into(viewHolder.examBackground);
        }


    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView examBackground;
        TextView examDay;
        TextView examMonthYear;
        TextView examTeachers;
        TextView examSubject;

        ViewHolder(View itemView) {
            super(itemView);
            examBackground = (ImageView) itemView.findViewById(R.id.exam_background);
            examDay = (TextView) itemView.findViewById(R.id.exam_day);
            examMonthYear = (TextView) itemView.findViewById(R.id.exam_month_year);
            examTeachers = (TextView) itemView.findViewById(R.id.exam_teachers);
            examSubject = (TextView) itemView.findViewById(R.id.exam_subject);
        }
    }
}
