package org.jeecg;


import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(value = {"classpath:application.yml"}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = JeecgSystemApplication.class)
@ActiveProfiles(profiles = "dev")
@AutoConfigureMockMvc
@Transactional
public abstract class BaseTest {

    /*  @Before
    public void init() {
        UserDetailDTO user = new UserDetailDTO();
        user.setName("TEST");
        user.setId(-1L);
        List<UserDetailDTO.UserCorpDTO> corps = new ArrayList<>();
        UserDetailDTO.UserCorpDTO corp =new
                UserDetailDTO.UserCorpDTO();
        corp.setCorpId(99742241);
        corp.setDef(true);
        corps.add(corp);
        corp =new
                UserDetailDTO.UserCorpDTO();
        corp.setCorpId(99724284);
        corps.add(corp);
        corp =new
                UserDetailDTO.UserCorpDTO();
        corp.setCorpId(99825281);
        corps.add(corp);
        user.setUserCorps(corps);
        UserContext.set(user);
    }*/
}