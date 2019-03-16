package org.trading.api.event;

import com.google.common.base.MoreObjects;
import org.trading.api.message.FillStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class MarketOrderRejected {

    public final UUID id;
    public final FillStatus fillStatus;
    public final LocalDateTime time;

    public MarketOrderRejected(UUID id,
                               FillStatus fillStatus,
                               LocalDateTime time) {
        this.id = id;
        this.fillStatus = fillStatus;
        this.time = time;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("fillStatus", fillStatus)
                .add("time", time)
                .toString();
    }
}
