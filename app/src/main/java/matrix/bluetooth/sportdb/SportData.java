package matrix.bluetooth.sportdb;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity(tableName = "sportdata")
public class SportData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    public int uid;
    @Embedded
    //@ColumnInfo(name = "date")
    public mDate date;
    @Embedded(prefix = "start")
    //@ColumnInfo(name = "start_time")
    public mTime startTime;
    @Embedded(prefix = "end")
    //@ColumnInfo(name = "end_time")
    public mTime endTime;
    @ColumnInfo(name = "step_count")
    public int stepCount;
    //@ColumnInfo(name = "accelerometer_data")
    //public float[] accelerometerData;
    public static class mDate {
        @ColumnInfo(name = "year")
        public int year;
        @ColumnInfo(name = "month")
        public int month;
        @ColumnInfo(name = "day")
        public int day;
        public  mDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        private String getFormat() {
            return year+"-"+month+"-"+day;
        }
    }

    public static class mTime {
        @ColumnInfo(name = "hour")
        public int hour;
        @ColumnInfo(name = "minute")
        public int minute;
        @ColumnInfo(name = "second")
        public int second;
        public mTime(int hour, int minute, int second) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

        public String getFormat() {
            return hour+":"+minute+":"+second;
        }
    }

    public SportData(mDate date, mTime startTime, mTime endTime, int stepCount){
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.stepCount = stepCount;
    }

    public String getDateFormat() {
        return date.getFormat();
    }

    public String getStartTimeFormat() {
        return startTime.getFormat();
    }

    public String getEndTimeFormat() {
        return  endTime.getFormat();
    }

    public mTime getDuration() {
        int dt = (endTime.hour * 3600 + endTime.minute * 60 + endTime.second ) - (startTime.hour * 3600 + startTime.minute * 60 + startTime.second);
        mTime time = new mTime(dt/3600, dt % 3600 / 60, dt % 60);
        return time;
    }

    public int getStepCount() {
        return stepCount;
    }
}
