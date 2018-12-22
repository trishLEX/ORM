package GenService.Postgres.Math.Tables;

import org.junit.jupiter.api.Test;
import ru.bmstu.ORM.Service.Session.Session;
import ru.bmstu.ORM.Service.Session.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class PowersTest {
    @Test
    public void testPowers() {
        SessionFactory factory = new SessionFactory("localhost", "5432", "postgres", "math", "0212");
        Session session = factory.openSession();
        List<Powers> powers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Powers power = new Powers();
            power.setCube(i * i * i);
            power.setSquare(i * i);
            power.setX(i);
            powers.add(power);
        }

        for (int i = 0; i < 100; i++) {
            session.save(powers.get(i));
        }
    }
}
