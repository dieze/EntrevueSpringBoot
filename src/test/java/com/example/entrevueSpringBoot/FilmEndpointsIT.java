package com.example.entrevueSpringBoot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.transaction.Transactional;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Black-box testing of {@link FilmController} endpoints.
 *
 * @see TransactionalTestExecutionListener handles @Transactional on Test class or @Test method
 * @see TransactionalTestExecutionListener#isDefaultRollback that rollbacks by default
 */
@IntegrationTest // @SpringBootTest with common configuration
@Transactional
// rollback by default: inserts are not actually committed
// tests can use the same film's titre and auteur's combination of nom & prenom
// TODO: is film's titre unique ?
// TODO: is auteur's combination of nom & prenom unique ?
public class FilmEndpointsIT {

    /**
     * @see #withIds(long[]) for insertions of ids for POST & GET response body assertions.
     */
    //language=json
    private static final String BODY =
            "" + // <- quick-fix to avoid IntelliJ formatter making a mess
                    // no space around ":" please! it is used in search-replace
                    "{\n" + // id will be inserted here based on titre TODO: is film's titre unique ?
                    "  \"titre\":\"Star Wars: The Empire Strikes Back\",\n" +
                    "  \"description\":\"Darth Vader is adamant about turning Luke Skywalker to the dark side.\",\n" +
                    "  \"acteurs\":[\n" +
                    "    {\n" + // id will be inserted here based on nom & prenom TODO: is acteur's combination of nom & prenom unique ?
                    "      \"nom\":\"Ford\",\n" +
                    "      \"prenom\":\"Harrison\"\n" +
                    "    },\n" +
                    "    {\n" + // id will be inserted here based on nom & prenom TODO: is acteur's combination of nom & prenom unique ?
                    "      \"nom\":\"Hamill\",\n" +
                    "      \"prenom\":\"Mark\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

    private static String withIds(long[] ids) { // 3: film & Ford Harrison & Hamill Mark
        // insert id of film
        // TODO: is film's titre unique ?
        String anchor = "  \"titre\":\"Star Wars: The Empire Strikes Back\",\n";
        if (!BODY.contains(anchor)) throw new RuntimeException(); // fail-safe
        String withIds = BODY.replace(anchor, String.format("  \"id\":%d,\n", ids[0]) + anchor);

        // insert id of acteur Ford Harrison
        // TODO: is acteur's combination of nom & prenom unique ?
        anchor = "      \"nom\":\"Ford\",\n";
        anchor += "      \"prenom\":\"Harrison\"\n";
        if (!BODY.contains(anchor)) throw new RuntimeException(); // fail-safe
        withIds = withIds.replace(anchor, String.format("      \"id\":%d,\n", ids[1]) + anchor);

        // insert id of acteur Hamill Mark
        // TODO: is acteur's combination of nom & prenom unique ?
        anchor = "      \"nom\":\"Hamill\",\n";
        anchor += "      \"prenom\":\"Mark\"\n";
        if (!BODY.contains(anchor)) throw new RuntimeException(); // fail-safe
        withIds = withIds.replace(anchor, String.format("      \"id\":%d,\n", ids[2]) + anchor);

        return withIds;
    }

    @Autowired
    private FilmTestRepository filmTestRepository;

    private long[] findIds() { // 3: film & Ford Harrison & Hamill Mark
        Long[] ids = new Long[3]; // Long[] might have null
        // TODO: is film's titre unique ?
        //noinspection OptionalGetWithoutIsPresent ; let it fail!
        Film film = filmTestRepository.findByTitre("Star Wars: The Empire Strikes Back").get();
        ids[0] = film.getId();
        assertNotNull(ids[0], "null id for film");

        // TODO: is acteur's combination of nom & prenom unique ?

        for (FilmActeur acteur : film.getActeurs()) {
            if ("Ford".equals(acteur.getNom()) && "Harrison".equals(acteur.getPrenom())) ids[1] = acteur.getId();
            if ("Hamill".equals(acteur.getNom()) && "Mark".equals(acteur.getPrenom())) ids[2] = acteur.getId();
        }

        assertNotNull(ids[1], "null id for acteur Ford Harrison");
        assertNotNull(ids[2], "null id for acteur Hamill Mark");

        return Arrays.stream(ids).mapToLong(Long::longValue).toArray(); // long[] do not have null
    }

    @Autowired
    private MockMvc mockMvc;

    private ResultActions doPostFilm() throws Exception {
        return doPostFilm(BODY);
    }

    private ResultActions doPostFilm(String body) throws Exception {
        return mockMvc.perform(
                post("/api/film").contentType("application/json").content(body)
        );
    }

    @Test
    public void postFilm_201() throws Exception {
        doPostFilm()
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(withIds(findIds()), true))
                ;
    }

    @Test
    public void postFilm_409() throws Exception {
        doPostFilm(); // titre: "Star Wars: The Empire Strikes Back"
        doPostFilm() // titre: "Star Wars: The Empire Strikes Back"
                .andExpect(status().isConflict()) // 409 Conflict
                .andExpect(content().contentTypeCompatibleWith("text/plain")) // #contentTypeCompatibleWith -> issue with ";charset=UTF-8"
                //.andExpect(content().string(org.hamcrest.Matchers.containsString("Unique index or primary key violation"))) // don't uncomment ; help for dev must not be tested
                ;
    }

    @Test
    public void postFilm_400() throws Exception {
        doPostFilm(BODY
                .replace("\"titre\":\"Star Wars: The Empire Strikes Back\",","")
                .replace("\"nom\":\"Ford\",", "")
        )
                .andExpect(status().isBadRequest())
                ;
    }

    @Test
    public void getFilm_200() throws Exception {
        doPostFilm();
        long[] ids = findIds();

        mockMvc.perform(
                get("/api/film/{id}", ids[0])
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(withIds(ids), true))
                ;
    }

    // not specified on the instructions
    // but it makes sense to respond 404 for non-existing film
    @Test
    public void getFilm_404() throws Exception {
        // film id is signed long (unsigned long in Java do not exist)
        // IDENTITY increment starts at 0
        // -1 is valid value and will always respond 404 Not Found
        mockMvc.perform(
                get("/api/film/{id}", -1)
        )
                .andExpect(status().isNotFound())
                .andExpect(content().string("")); // empty response body
    }
}
