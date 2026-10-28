package com.familiemunshi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret=dGVzdC1vbmx5LXNlY3JldC1rZXktMzItYnl0ZXMtbG9uZyEh"
})
class FamiliemunshiApplicationTests {

    @Test
    void contextLoads() {
    }

}
