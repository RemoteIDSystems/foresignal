package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** The {@code ticket} parameter never reaches anything past the filter (prompt 023). */
class TicketStrippingRequestTest {

    @Test
    @DisplayName("the ticket is read from the query string and removed from every view of the request")
    void strips() {
        MockHttpServletRequest original = new MockHttpServletRequest("GET", "/v1/tiles.json");
        original.setQueryString("a=1&ticket=fst_abc&b=2");
        original.addParameter("a", "1");
        original.addParameter("ticket", "fst_abc");
        original.addParameter("b", "2");

        TicketStrippingRequest request = new TicketStrippingRequest(original);

        assertThat(TicketStrippingRequest.ticket(original.getQueryString())).isEqualTo("fst_abc");
        assertThat(request.getQueryString()).isEqualTo("a=1&b=2");
        assertThat(request.getParameter("ticket")).isNull();
        assertThat(request.getParameterValues("ticket")).isNull();
        assertThat(request.getParameterMap()).containsOnlyKeys("a", "b");
        assertThat(java.util.Collections.list(request.getParameterNames())).containsExactly("a", "b");
        assertThat(request.getParameter("a")).isEqualTo("1");
    }

    @Test
    @DisplayName("edge cases: no query, only a ticket, an encoded ticket, a malformed escape")
    void edges() {
        assertThat(TicketStrippingRequest.strip(null)).isNull();
        assertThat(TicketStrippingRequest.strip("ticket=x")).isNull();
        assertThat(TicketStrippingRequest.strip("tickets=1")).isEqualTo("tickets=1");
        assertThat(TicketStrippingRequest.ticket("ticket=fst_a%2Db")).isEqualTo("fst_a-b");
        assertThat(TicketStrippingRequest.ticket("ticket=%zz")).isNull();
        assertThat(TicketStrippingRequest.ticket("ticket=")).isNull();
        assertThat(TicketStrippingRequest.ticket("x=1")).isNull();
        assertThat(TicketStrippingRequest.ticket(null)).isNull();
    }
}
