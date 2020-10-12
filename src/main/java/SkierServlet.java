import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
    private final String POST_PATH_LIFTRIDES = "liftrides";
    private final String GET_PATH_DAY = "days";
    private final String GET_PATH_SKIERS = "skiers";
    private final String GET_PATH_VERTICAL = "vertical";
    private final String RESORT_PARAM = "resort";
    private final String MISSING_RESORT_PARAM_MSG = "{ \"message\": \"Invalid Params\" }";
    private final String TOTAL_VERT_SUCCESS_MSG = "{\"resort\": \" %1$s\",\"totalVertical\": %2$d}";
    private final int GET_RESORT_DAY_SKIER_MAX_ELEMENTS = 6;
    private final int GET_VERTICAL_MAX_ELEMENTS = 3;
    private final int MIN_NUM_ELEMENTS_IN_LIFTRIDES_ENDPOINT = 2;
    private int DUMMY_VERTICAL = 123459876;
    private int DUMMY_VERT_DAY = 99999;

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Optional<String[]> possiblePath = ServletUtil.processRequest(req, res);

        if (possiblePath.isPresent()) {
            String[] pathElements = possiblePath.get();
            if (hasValidPostEndpoint(pathElements)) {
                res.setStatus(HttpServletResponse.SC_CREATED);
                successfulPostBehavior(req, res);
            } else {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, ServletUtil.INVALID_PATH_STATUS_MSG);
            }
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, ServletUtil.EMPTY_PATH_STATUS_MSG);
        }
    }

    private boolean hasValidPostEndpoint(String[] pathElements) {
        return pathElements.length == MIN_NUM_ELEMENTS_IN_LIFTRIDES_ENDPOINT
                && POST_PATH_LIFTRIDES.equals(pathElements[1]);
    }

    private void successfulPostBehavior(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        try (BufferedReader reader = req.getReader();
             PrintWriter writer = res.getWriter()) {

            String requestInfo = reader.readLine();
            while (requestInfo != null) {
                writer.println(requestInfo);
                requestInfo = reader.readLine();
            }
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Optional<String[]> possiblePath = ServletUtil.processRequest(req, res);
        if (possiblePath.isPresent()) {
            String[] pathElements = possiblePath.get();

            if (isTotalVerticalPath(pathElements)) {
                handleResortParam(req, res, pathElements);
            } else if (isVerticalForResortDayPath(pathElements)) {
                handleResortDaySkierParams(res, pathElements);
            }
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(ServletUtil.INVALID_PATH_STATUS_MSG);
        }
    }

    private void handleResortParam(HttpServletRequest req, HttpServletResponse res, String[] pathElements)
            throws IOException {
        String resortParam = req.getParameter(RESORT_PARAM);
        if (resortParam == null || resortParam.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(MISSING_RESORT_PARAM_MSG);
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(String.format(TOTAL_VERT_SUCCESS_MSG, resortParam, DUMMY_VERTICAL));
        }
    }

    private void handleResortDaySkierParams(HttpServletResponse res, String[] pathElements)
            throws IOException {
        String resortId = pathElements[1];
        String dayId = pathElements[3];
        String skierId = pathElements[5];

        boolean allNonNullAndNotEmpty =
                resortId != null && !resortId.isEmpty()
                        && dayId != null && !dayId.isEmpty()
                        && skierId != null && !skierId.isEmpty();

        if (allNonNullAndNotEmpty && ServletUtil.isValidDay(dayId)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(String.format(TOTAL_VERT_SUCCESS_MSG, resortId, DUMMY_VERT_DAY));
        } else {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, ServletUtil.INVALID_PATH_STATUS_MSG);
        }
    }

    private boolean isTotalVerticalPath(String[] pathElements) {
        if (GET_VERTICAL_MAX_ELEMENTS == pathElements.length) {
            return GET_PATH_VERTICAL.equals(pathElements[2]);
        }
        return false;
    }

    private boolean isVerticalForResortDayPath(String[] pathElements) {
        if (GET_RESORT_DAY_SKIER_MAX_ELEMENTS == pathElements.length) {
            return GET_PATH_DAY.equals(pathElements[2])
                    && GET_PATH_SKIERS.equals(pathElements[4]);
        }
        return false;
    }
}
