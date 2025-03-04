package matrix.bluetooth.sportdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
@Database(entities = {SportData.class}, version = 1, exportSchema=false)
public abstract class SportDatabase extends RoomDatabase {

    private static volatile SportDatabase sportDatabase;

    static SportDatabase getInstance(Context context) {
        if (sportDatabase == null) {
            synchronized (SportDatabase.class) {
                if (sportDatabase == null) {
                    sportDatabase = Room.databaseBuilder(context.getApplicationContext(), SportDatabase.class, "dbRoomTest.db")
                            .addMigrations()
                            // 默认不允许在主线程中连接数据库
                            // .allowMainThreadQueries()
                            .build();
                }
            }
        }

        return sportDatabase;
    }

    public abstract SportDataDao sportDataDao();

}
