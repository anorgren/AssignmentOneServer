package data.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LiftRide {
    private String skierId;
    private String liftId;
    private String resortId;
    private String dayId;
    private int vertical;
    private String time;
}
