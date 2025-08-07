import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ListOfReposIntegrationTest {

    @Test
    void shouldFetchOwnerReposBranchesAndShaCorrectly() throws Exception {
        // given
        String username = "torvalds"; // known, public GitHub user
        ListOfRepos listOfRepos = new ListOfRepos();

        // capture System.out output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // when
        int result = listOfRepos.getRepos(username);

        // then
        System.setOut(originalOut); // restore System.out
        String output = outContent.toString();

        // === FULL VALIDATION ===
        assertEquals(0, result, "Method should return 0 for a valid user");

        // 1. Check that the owner is printed
        assertTrue(output.contains("Owner: torvalds"), "Owner should be printed");

        // 2. Check that repositories are printed
        assertTrue(output.contains("Repository #"), "Repositories should be printed");

        // 3. Check that each branch line contains a SHA
        Pattern branchPattern = Pattern.compile("  - Branch: ([^,]+), SHA: ([a-f0-9]{40})");
        Matcher matcher = branchPattern.matcher(output);
        int shaCount = 0;
        while (matcher.find()) {
            shaCount++;
            String sha = matcher.group(2);
            assertEquals(40, sha.length(), "SHA should be 40 characters long");
        }

        assertTrue(shaCount > 0, "At least one SHA should be found");

        // 4. Check that there are no fetch errors
        assertFalse(output.contains("Failed to fetch"), "There should be no fetch errors");
    }
}
