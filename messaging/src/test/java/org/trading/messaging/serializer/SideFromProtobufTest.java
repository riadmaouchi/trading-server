package org.trading.messaging.serializer;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.api.command.Side;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.Side.UNKNOWN_SIDE;


@ExtendWith(DataProviderExtension.class)
class SideFromProtobufTest {

    private SideFromProtobuf sideFromProtobuf;

    @BeforeEach
    void before() {
        sideFromProtobuf = new SideFromProtobuf();
    }

    @DataProvider({
            "BUY, BUY",
            "SELL, SELL",
    })
    @TestTemplate
    void should_convert_order_side(org.trading.Side inputSide, Side outputSide) {

        // When
        Either<String, Side> sideEither = sideFromProtobuf.visit(inputSide, inputSide);

        // then
        Side side = sideEither.right();
        assertThat(side).isEqualTo(outputSide);
    }


    @Test
    void should_fail_to_convert_unknown_order_side() {

        // When
        Either<String, Side> sideEither = sideFromProtobuf.visit(UNKNOWN_SIDE, UNKNOWN_SIDE);

        // then
        String reason = sideEither.left();
        assertThat(reason).isEqualTo("Unknown side : UNKNOWN_SIDE");
    }

}