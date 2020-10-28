package data.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LiftRide {
    private String skierID;
    private String liftID;
    private String resortID;
    private String dayID;
    private int vertical;
    private String time;
}
