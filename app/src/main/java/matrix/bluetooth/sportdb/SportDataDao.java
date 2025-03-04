package matrix.bluetooth.sportdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SportDataDao {

    @Insert
    void insertSportData(SportData sportData);
    @Insert
    void insertSportData(List<SportData> sportData);
    @Insert
    void insertSportData(SportData... sportData);
    @Delete
    void delete(SportData sportData);
    @Delete
    void deleteSportData(SportData... sportData);
    @Update
    void updateSportData(SportData sportData);
    @Update
    void updateSportData(SportData... sportData);
    @Query("SELECT * FROM sportdata order by starthour")
    List<SportData> loadAll();

    @Query("SELECT * FROM sportdata WHERE uid=(:uid)")
    SportData findUserById(int uid);

    @Query("SELECT * FROM sportdata WHERE uid IN (:uids)")
    List<SportData> loadAllByIds(int[] uids);

    @Query("SELECT * FROM sportdata WHERE year LIKE :year AND month LIKE :month AND day LIKE :day")
    SportData loadAllByDate(int year, int month, int day);

    @Query("delete from sportdata")
    void deleteSportData();

}
