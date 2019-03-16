package org.trading.messaging.serializer;

import com.google.protobuf.Timestamp;
import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.MessageProvider;
import org.trading.api.event.MarketOrderRejected;
import org.trading.api.message.FillStatus;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.MessageProvider.FillStatus.UNKNOWN_FILL_STATUS;
import static org.trading.messaging.MarketOrderRejectedBuilder.aMarketOrderRejected;

@ExtendWith(DataProviderExtension.class)
class MarketOrderRejectedFromProtobufTest {

    private MarketOrderRejectedFromProtobuf marketOrderRejectedFromProtobuf;

    @BeforeEach
    void before() {
        marketOrderRejectedFromProtobuf = new MarketOrderRejectedFromProtobuf();
    }

    @Test
    void should_convert_market_order_rejected() {

        // Given
        MessageProvider.MarketOrderRejected marketOrderRejected = aMarketOrderRejected()
                .withId("00000000-0000-0001-0000-000000000002")
                .withTime(Timestamp.newBuilder().setSeconds(1530464460L).build())
                .build();

        // When
        Either<String, MarketOrderRejected> marketOrderRejectedEither = marketOrderRejectedFromProtobuf.fromProtobuf(marketOrderRejected);

        // then
        MarketOrderRejected orderRejected = marketOrderRejectedEither.right();
        assertThat(orderRejected.id).isEqualTo(new UUID(1, 2));
        assertThat(orderRejected.time).isEqualTo(LocalDateTime.of(2018, JULY, 1, 17, 1));
    }

    @DataProvider({
            "FULLY_FILLED, FULLY_FILLED",
            "PARTIALLY_FILLED, PARTIALLY_FILLED",
    })
    @TestTemplate
    void should_convert_order_fill_status(MessageProvider.FillStatus inputFillStatus, FillStatus outputFillStatus) {

        // Given
        MessageProvider.MarketOrderRejected marketOrderRejected = aMarketOrderRejected()
                .withFillStatus(inputFillStatus)
                .build();

        // When
        Either<String, MarketOrderRejected> marketOrderRejectedEither = marketOrderRejectedFromProtobuf.fromProtobuf(marketOrderRejected);

        // then
        FillStatus fillStatus = marketOrderRejectedEither.right().fillStatus;
        assertThat(fillStatus).isEqualTo(outputFillStatus);
    }

    @Test
    void should_fail_to_convert_unknown_order_fill_status() {

        // Given
        MessageProvider.MarketOrderRejected marketOrderRejected = aMarketOrderRejected()
                .withFillStatus(UNKNOWN_FILL_STATUS)
                .build();

        // When
        Either<String, MarketOrderRejected> marketOrderRejectedEither = marketOrderRejectedFromProtobuf.fromProtobuf(marketOrderRejected);


        // then
        String reason = marketOrderRejectedEither.left();
        assertThat(reason).isEqualTo("Unknown fill status : UNKNOWN_FILL_STATUS");
    }

}