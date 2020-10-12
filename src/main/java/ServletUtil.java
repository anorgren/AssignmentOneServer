import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class ServletUtil {
    public static final String INVALID_PATH_STATUS_MSG = "\"message:\" \"Failed: Invalid Path\"";
    public static final String EMPTY_PATH_STATUS_MSG = "\"message:\" \"Failed: Missing Path Variables\"";

    private static boolean isNonEmptyPath(HttpServletRequest req) {
        final String requestedUrl = req.getPathInfo();
        return requestedUrl != null && !requestedUrl.isEmpty();
    }

    public static void setResponseValues(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json;charset=UTF-8");

        if (!isNonEmptyPath(req)) {
            res.getWriter().write(INVALID_PATH_STATUS_MSG);
        }
    }

    public static Optional<String[]> processRequest(HttpServletRequest req, HttpServletResponse res)
            throws IOException{
        setResponseValues(req, res);

        if (isNonEmptyPath(req)) {
            String[] pathElements = req.getPathInfo().split("/");
            return Optional.of(pathElements);
        }

        return Optional.empty();
    }

    public static Optional<Integer> getIntegerFromElement(String pathElement) {
        try {
            Integer result = Integer.parseInt(pathElement);
            return Optional.of(result);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static boolean isValidDay(String day) {
        Optional<Integer> possibleDay = ServletUtil.getIntegerFromElement(day);
        if (possibleDay.isPresent()) {
            int dayAsInt = possibleDay.get();
            return 1 <= dayAsInt && dayAsInt <= 366;
        }
        return false;
    }
}
