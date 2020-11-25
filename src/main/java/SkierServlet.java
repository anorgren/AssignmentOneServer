import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import data.SkierDao;
import data.model.LiftRide;
import data.model.SkierVertical;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {

    private final String POST_PATH_LIFTRIDES = "liftrides";
    private final String GET_PATH_DAY = "days";
    private final String GET_PATH_SKIERS = "skiers";
    private final String GET_PATH_VERTICAL = "vertical";
    private final String RESORT_PARAM = "resort";
    private final String MISSING_RESORT_PARAM_MSG = "{ \"message\": \"Invalid Params\" }";
    private final String NOT_FOUND_MSG = "{ \"message\": \"Not Found\" }";
    private final int GET_RESORT_DAY_SKIER_MAX_ELEMENTS = 6;
    private final int GET_VERTICAL_MAX_ELEMENTS = 3;
    private final int MIN_NUM_ELEMENTS_IN_LIFTRIDES_ENDPOINT = 2;
    private int VERTICAL_MULTIPLIER = 10;
    private final String QUEUE_NAME_PERSISTENT = "SKIER_WRITE_QUEUE_PERSISTENT";
    private final String QUEUE_NAME_NOT_PERSISTENT = "SKIER_WRITE_QUEUE_NOT_PERSISTENT";
    private static final String HOST = "amqps://b-54863bfa-426a-425f-991b-9c83bd8fb830.mq.us-west-2.amazonaws.com:5671";

    private Connection queueConnection;
    private ObjectPool<Channel> channelPool;

    public void init() throws ServletException {
        super.init();
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(HOST);
            connectionFactory.setPort(5671);
            connectionFactory.setUsername("admin");
            connectionFactory.setPassword("password12345");

            queueConnection = connectionFactory.newConnection();
        } catch (Exception e) {
            System.err.println("Unable to establish connection");
            e.printStackTrace();
        }

        channelPool = new GenericObjectPool<>(new ChannelFactory(QUEUE_NAME_NOT_PERSISTENT, queueConnection));
    }

    public void destroy() {
        super.destroy();
        try {
            queueConnection.close();
            channelPool.close();
        } catch (Exception e) {
            System.err.println("Unable to destroy");
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        Optional<String[]> possiblePath = ServletUtil.processRequest(req, res);

        if (possiblePath.isPresent()) {
            String[] pathElements = possiblePath.get();
            if (hasValidPostEndpoint(pathElements)) {

                String reqBody = req.getReader().lines().collect(Collectors.joining());
                LiftRide liftRide = convertPostBodyToLiftRideObject(reqBody);

                String postMessage = createPostMessage(liftRide);

                try {
                    Channel channel = channelPool.borrowObject();

                    channel.basicPublish("",
                            QUEUE_NAME_NOT_PERSISTENT,
                            MessageProperties.PERSISTENT_TEXT_PLAIN,
                            postMessage.getBytes());

                    channelPool.returnObject(channel);
                    successfulPostBehavior(req, res);
                } catch (Exception e) {
                    res.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                }
            } else {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, ServletUtil.INVALID_PATH_STATUS_MSG);
            }
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, ServletUtil.EMPTY_PATH_STATUS_MSG);
        }
    }

    private String createPostMessage(LiftRide liftRide) {
        return String.format("%s,%s,%s,%s,%s,%d",
                liftRide.getResortID(),
                liftRide.getDayID(),
                liftRide.getSkierID(),
                liftRide.getTime(),
                liftRide.getLiftID(),
                liftRide.getVertical());
    }

    private boolean hasValidPostEndpoint(String[] pathElements) {
        return pathElements.length == MIN_NUM_ELEMENTS_IN_LIFTRIDES_ENDPOINT
                && POST_PATH_LIFTRIDES.equals(pathElements[1]);
    }

    private void successfulPostBehavior(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setStatus(HttpServletResponse.SC_CREATED);

        try (BufferedReader reader = req.getReader();
             PrintWriter writer = res.getWriter()) {

            String requestInfo = reader.readLine();
            while (requestInfo != null) {
                writer.println(requestInfo);
                requestInfo = reader.readLine();
            }
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Optional<String[]> possiblePath = ServletUtil.processRequest(req, res);
        if (possiblePath.isPresent()) {
            String[] pathElements = possiblePath.get();

            if (isTotalVerticalPath(pathElements)) {
                handleResortParam(req, res, pathElements);
            } else if (isVerticalForResortDayPath(pathElements)) {
                handleResortDaySkierParams(res, pathElements);
            }
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(ServletUtil.INVALID_PATH_STATUS_MSG);
        }
    }

    private void handleResortParam(HttpServletRequest req, HttpServletResponse res, String[] pathElements)
            throws IOException {
        String resortParam = req.getParameter(RESORT_PARAM);
        String skierId = pathElements[1];

        if (resortParam == null || resortParam.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(MISSING_RESORT_PARAM_MSG);
        } else {
            SkierVertical skierVertical =
                    (new SkierDao()).selectSkierVerticalResortTotal(skierId, resortParam);
            handleGetResponse(res, skierVertical);
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
            SkierVertical skierVertical =
                    (new SkierDao()).selectSkierTotalVerticalForDay(skierId, resortId, dayId);
            handleGetResponse(res, skierVertical);
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

    private LiftRide convertPostBodyToLiftRideObject(String jsonBody) {
        Gson gson = new Gson();
        LiftRide liftRide = gson.fromJson(jsonBody, LiftRide.class);
        liftRide.setVertical(calculateVertical(liftRide));

        return liftRide;
    }

    private int calculateVertical(LiftRide liftRide) {
        return Integer.parseInt(liftRide.getLiftID()) * VERTICAL_MULTIPLIER;
    }

    private void handleGetResponse(HttpServletResponse res, SkierVertical skierVertical) throws IOException {
        if (skierVertical == null) {
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
            res.getWriter().write(NOT_FOUND_MSG);
        } else {
            Gson gson = new Gson();
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(gson.toJson(skierVertical));
        }
    }
}
