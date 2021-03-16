package com.example.surdo.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.surdo.R;

@Database(entities = {Command.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CommandDao CommandDao();

    public void initializeDB() {
        this.CommandDao().deleteAll();
        this.CommandDao().insert(new Command("поворот направо", R.raw.turn_right));
        this.CommandDao().insert(new Command("поворот налево", R.raw.turn_left));
        this.CommandDao().insert(new Command("тормоз", R.raw.brake));
        this.CommandDao().insert(new Command("газ", R.raw.gas));
        this.CommandDao().insert(new Command("автомобиль", R.raw.car));

        this.CommandDao().insert(new Command("включи левый поворотник", R.raw.turn_on_the_left_turn_signal));
        this.CommandDao().insert(new Command("включи правый поворотник", R.raw.turn_on_the_right_turn_signal));
        this.CommandDao().insert(new Command("быстрее", R.raw.faster));
        this.CommandDao().insert(new Command("быстро", R.raw.fast));
        this.CommandDao().insert(new Command("нажми газ", R.raw.press_the_gas));

        this.CommandDao().insert(new Command("до перекрестка", R.raw.to_the_crossroads));
        this.CommandDao().insert(new Command("дорога", R.raw.road));
        this.CommandDao().insert(new Command("едем прямо", R.raw.going_straight));
        this.CommandDao().insert(new Command("задний ход", R.raw.reverse));
        this.CommandDao().insert(new Command("выключи левый поворотник", R.raw.turn_off_the_left_turn_signal));

        this.CommandDao().insert(new Command("можно", R.raw.can));
        this.CommandDao().insert(new Command("начинаем движение", R.raw.start_moving));
        this.CommandDao().insert(new Command("нельзя", R.raw.must_not));
        this.CommandDao().insert(new Command("необозначенный перекресток", R.raw.unmarked_intersection));
        this.CommandDao().insert(new Command("нерегулируемый перекресток", R.raw.unregulated_intersection));

        this.CommandDao().insert(new Command("не сдал", R.raw.did_not_pass));
        this.CommandDao().insert(new Command("не торопись", R.raw.do_not_rush));
        this.CommandDao().insert(new Command("нужно", R.raw.need));
        this.CommandDao().insert(new Command("обозначенный перекресток", R.raw.designated_intersection));
        this.CommandDao().insert(new Command("перестроение", R.raw.lane_change));

        this.CommandDao().insert(new Command("пешеходный переход", R.raw.crosswalk));
        this.CommandDao().insert(new Command("подними сцепление", R.raw.lift_the_clutch));
        this.CommandDao().insert(new Command("помедленнее", R.raw.slow_down));
        this.CommandDao().insert(new Command("после перекрестка", R.raw.after_the_crossroads));
        this.CommandDao().insert(new Command("выключи правый поворотник", R.raw.turn_off_the_right_turn_signal));

        this.CommandDao().insert(new Command("притормози", R.raw.slow_down_1));
        this.CommandDao().insert(new Command("разворот", R.raw.reversal));
        this.CommandDao().insert(new Command("разрешено", R.raw.allowed));
        this.CommandDao().insert(new Command("регулируемый перекресток", R.raw.regulated_intersection));
        this.CommandDao().insert(new Command("светофор", R.raw.traffic_light));

        this.CommandDao().insert(new Command("сдал", R.raw.passed));
        this.CommandDao().insert(new Command("смотри на дорогу", R.raw.look_at_the_road));
        this.CommandDao().insert(new Command("выжми сцепление", R.raw.squeeze_the_clutch));
        this.CommandDao().insert(new Command("съезд с дороги", R.raw.exit_from_the_road));
        this.CommandDao().insert(new Command("выключи фары", R.raw.turn_off_the_headlights));
        this.CommandDao().insert(new Command("чуть-чуть быстрее", R.raw.a_little_bit_faster));
    }
}