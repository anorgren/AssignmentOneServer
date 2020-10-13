import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ResortServlet")
public class ResortServlet extends HttpServlet {
    private final String RESORT_PARAM = "resort";
    private final String DAY_ID_PARAM = "dayID";
    private final String URL_DAY = "day";
    private final String URL_TOP_TEN = "top10vert";
    private final String DUMMY_TOP_TEN = "{\"topTenSkiers\": [{\"skierID\": 1231, " +
            "\"verticalTotal\": 1777}, {\"skierID\": 222, \"verticalTotal\": 23423}]}";
    private final int DAY_PATH_LOC = 1;
    private final int TOP_TEN_PATH_LOC = 3;

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        ServletUtil.setResponseValues(req, res);

        String resort = req.getParameter(RESORT_PARAM);
        String dayId = req.getParameter(DAY_ID_PARAM);
        String[] pathElements = req.getPathInfo().split("/");

        boolean resortPresent = resort != null && !resort.isEmpty();
        boolean dayIdPresent = dayId != null && !dayId.isEmpty();

        if (resortPresent && dayIdPresent && isTopTenPath(pathElements)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(DUMMY_TOP_TEN);
        } else {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, ServletUtil.INVALID_PATH_STATUS_MSG);
        }
    }

    private boolean isTopTenPath(String[] pathElements) {
        return pathElements.length >= 3
                && pathElements[DAY_PATH_LOC].equals(URL_DAY)
                && pathElements[TOP_TEN_PATH_LOC].equals(URL_TOP_TEN);
    }
}
