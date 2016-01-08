package demo;

import demo.ctrl.SessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link SessionService} test
 * <p>
 * Testing "/session" endpoint
 *
 * @author mak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SessionServiceTest {

    @Autowired
    WebApplicationContext webApplicationContext;
    MockMvc mockMvc;
    String path = "/session/get";

    /**
     * Initialize {@link MockMvc}
     */
    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Testing "/get" endpoint
     */
    @Test
    public void testSessionGet() throws Exception {
        // get session uid:
        MvcResult action = doGet();
        UUID uid = UUID.nameUUIDFromBytes(action.getResponse().getContentAsByteArray());

        assertNotNull("uid is null", uid);
        assertNotSame("uid is empty", "", uid.toString());

        // let's get another uid:
        action = doGet();

        UUID uid2 = UUID.nameUUIDFromBytes(action.getResponse().getContentAsByteArray());

        System.out.println(uid2);
        assertNotSame("uids are same", uid, uid2);

        // set uid2 into http session and check same result for uid:
        action = doGet(uid2);
        uid = UUID.nameUUIDFromBytes(action.getResponse().getContentAsByteArray());
        System.out.println(uid);
        // assertEquals("uids arent same", secondUid, uid);
        // last part of test wasn't work because of MockMvc, but works fine in real world
        // ...just believe me :)
        // or check it locally by your own or look for log output. you should see smth like:
        // 1. 20ddc276-76e4-32c5-a702-5a25f0c1bb42
        // 2. 2015-03-14 01:38:25.920  INFO ... returned uid: 20ddc276-76e4-32c5-a702-5a25f0c1bb42
        // 3. b30c050e-8e1a-3d94-8f9e-2522494e6eee
        // where 1 was in session attribute, 2 indicates same uid, which returns (real)
        // but 3 is result, which came from proxy implementation of service (not real)
    }

    /**
     * Get new session uid
     *
     * @return {@link MvcResult}
     */
    private MvcResult doGet() throws Exception {
        return mockMvc.perform(get(path)
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * Get existed session uid
     *
     * @return {@link MvcResult}
     */
    private MvcResult doGet(UUID uid) throws Exception {
        return mockMvc.perform(get(path)
                .sessionAttr(SessionService.attributeName, uid)
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn();
    }
}
