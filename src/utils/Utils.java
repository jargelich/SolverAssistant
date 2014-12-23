/*
 * TFG: Daniel Casanovas Gayoso 
 * Grau en Enginyeria Informàtica - Escola Politècnica Superior de Lleida
 * 2014/2015
 * Utils
 */
package utils;

import entities.CompareSolver;
import entities.SolverInstance;
import entities.Solver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import solverassistant.SolverManager;

public class Utils {

    public Utils() {
    }

    public String fileReader(File file) {
        String content = "";
        FileReader fr = null;
        BufferedReader br = null;
        try {
            // Opening File
            fr = new FileReader(file);
            br = new BufferedReader(fr);

            // Reading File
            String linea;
            while ((linea = br.readLine()) != null) {
                content += linea;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fr) {
                    fr.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return content;
    }

    public static void fileWriter(String path, String content) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "utf-8"));
            writer.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
    }

    public static Solver createSolverFromData(String logName, String log) {
        Solver solverCharged = new Solver();
        try {
            List<String> solverInfoList = Arrays.asList(logName.split("-"));
            Collections.reverse(solverInfoList);
            solverCharged.setNumberOfCores(Integer.parseInt((solverInfoList.get(0)).substring(0, (solverInfoList.get(0)).lastIndexOf("."))));
            solverCharged.setMemory(Integer.parseInt(solverInfoList.get(1)));
            solverCharged.setTimeOut(Integer.parseInt(solverInfoList.get(2)));
            solverCharged.setType(solverInfoList.get(3));
            solverCharged.setBenchmark(solverInfoList.get(4));
            solverCharged.setName(solverInfoList.get(solverInfoList.size() - 1));
        } catch (Exception e) {
            System.err.println("[ERROR-INFO] Solver has incomplete or incorrect name.");
            return null;
        }
        try {
            StringTokenizer st = new StringTokenizer(log, "\t");
            SolverInstance instance = null;
            boolean firstElement = true;
            while (st.hasMoreTokens()) {
                if (firstElement) {
                    instance = new SolverInstance();
                    instance.setFileName(st.nextToken());
                    firstElement = false;
                }
                instance.setTime(Double.parseDouble(st.nextToken()));
                instance.setSolution(st.nextToken());
                instance.setOptimum(Integer.parseInt(st.nextToken()));
                instance.setInfo(Integer.parseInt(st.nextToken()));
                instance.setTimeOut(Integer.parseInt(st.nextToken()));
                instance.setBuggy(Integer.parseInt(st.nextToken()));
                instance.setSegmentationFault(Integer.parseInt(st.nextToken()));
                instance.setOutOfMemory(Integer.parseInt(st.nextToken()));
                instance.setLog(st.nextToken());
                instance.setNumberOfVariables(Integer.parseInt(st.nextToken()));
                instance.setNumberOfClause(Integer.parseInt(st.nextToken()));
                instance.setNumberOfHardClause(Integer.parseInt(st.nextToken()));
                instance.setNumberOfSoftClause(Integer.parseInt(st.nextToken()));
                instance.setNumberOfUnsatClause(Integer.parseInt(st.nextToken()));

                String unSatClauseWeigthString = st.nextToken();
                int limit = unSatClauseWeigthString.indexOf("/", 0);
                if (limit != -1) {
                    instance.setUnsatClauseWeigth(Integer.parseInt(unSatClauseWeigthString.substring(0, limit)));
                } else {
                    instance.setUnsatClauseWeigth(Integer.parseInt(unSatClauseWeigthString));
                }

                solverCharged.addInstanceToList(instance);

                if (st.hasMoreTokens()) {
                    instance = new SolverInstance();
                    instance.setFileName(unSatClauseWeigthString.substring(limit));
                }
            }
        } catch (Exception e) {
            System.err.println("[WARNING-INFO] There is incomplete or incorrect instances in the log.");
        } finally {
            return solverCharged;
        }
    }

    public static Map<String, CompareSolver> getComparisonData(List<Solver> solvers) {

        loadInstancesLists(solvers); // Charge all instances into solvers from DAO
        Map<String, CompareSolver> map = new LinkedHashMap<>();

        for (Solver solv : solvers) {
            int instancesCount = 0;
            List<Double> timeValues = new ArrayList<Double>();
            String folder = getFolder(solv.getInstancesList().get(0));
            for (SolverInstance instance : solv.getInstancesList()) {
                if (folder.equals(getFolder(instance))) { // While we don't change the folder increment counters
                    instancesCount++; // Increment instances count
                    if (isSolved(instance)) { // If the instance is solved save the time and increment de solved count
                        timeValues.add(instance.getTime());
                    }
                } else { // If not save the data and reset the counters
                    // Save Data
                    ArrayList<Double> info = new ArrayList<>(); // Object 0 is the number of solved instances, 1 the mean , 2 the median
                    info.add((double) timeValues.size());
                    info.add(mean(timeValues));
                    info.add(median(timeValues));

                    CompareSolver solverToAdd;
                    if (map.containsKey(folder)) {
                        solverToAdd = map.get(folder);
                    } else {
                        solverToAdd = new CompareSolver(folder);
                    }

                    solverToAdd.addToHashMap(solv.getName(), info); // Add to CompareSolver object hashmap the data with the solver name as key
                    solverToAdd.setNumberOfInstances(instancesCount);

                    map.put(folder, solverToAdd); // Add to the map to return this object with the folder as key

                    // Reset Data and check the first instance of the new solver
                    folder = getFolder(instance);
                    instancesCount = 1;
                    timeValues = new ArrayList<>();
                    if (isSolved(instance)) { // If the instance is solved save the time and increment de solved count
                        timeValues.add(instance.getTime());
                    }
                }
            }
            // Save Last Folder Data
            ArrayList<Double> info = new ArrayList<>();
            info.add((double) timeValues.size());
            info.add(mean(timeValues));
            info.add(median(timeValues));
            CompareSolver solverToAdd;
            if (map.containsKey(folder)) {
                solverToAdd = map.get(folder);
            } else {
                solverToAdd = new CompareSolver(folder);
            }

            solverToAdd.addToHashMap(solv.getName(), info);
            solverToAdd.setNumberOfInstances(instancesCount);

            map.put(folder, solverToAdd);
        }
        return map;
    }

    private static void loadInstancesLists(List<Solver> solvers) {
        for (Solver solv : solvers) {
            solv.setInstancesList(SolverManager.daoSolver.getAllInstancesBySolver(solv.getId()));
        }
    }

    private static String getFolder(SolverInstance instance) {
        StringTokenizer st;
        st = new StringTokenizer(instance.getFileName(), "/");
        for (int i = 1; i <= st.countTokens(); i++) {
            st.nextToken();
        }
        return st.nextToken() + "/" + st.nextToken();
    }

    public static Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    private static boolean isSolved(SolverInstance instance) {
        if (instance.getTimeOut() != 0 && instance.getBuggy() != 0 && instance.getSegmentationFault() != 0 && instance.getOutOfMemory() != 0) {
            return false;
        }
        try {
            if (Integer.parseInt(instance.getSolution()) == -1) {
                return false;
            }
        } catch (NumberFormatException e) {
//            System.err.println("[WARNING-INFO] Instance solution is not a Number.");
        }
        if (instance.getSolution().equals("UNKNOW")) {
            return false;
        }
        if (!(instance.getOptimum() == -1 || instance.getSolution().equals("OPTIMUM_FOUND") || instance.getSolution().equals("UNSAT") || instance.getSolution().equals("INCOMPLETE"))) {
            return false;
        }
        if (instance.getOptimum() == -1) {
            if (!instance.getSolution().equals("UNSAT")) {
                return false;
            }
        }
        return true;
    }

    private static Double mean(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        } else {
            double total = 0;
            for (double d : values) {
                total += d;
            }
            return total / values.size();
        }
    }

    private static Double median(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        } else {
            Collections.sort(values);
            int middle = values.size() / 2;
            if (values.size() % 2 == 1) {
                return values.get(middle);
            } else {
                return (values.get(middle - 1) + values.get(middle)) / 2.0;
            }
        }
    }

    public static void exportAsHTML(GridPane table, int columns, int rows) {
        String html = "<html><body><table><tr>";
        for (int i = 0; i < columns; i++) {
            String aux = getNodeFromGridPane(table, i, 0).toString();
            html += "<th>" + aux.substring(aux.indexOf("'") + 1, aux.lastIndexOf("'")) + "</th>";
        }
        html += "</tr>";
        for (int x = 1; x < rows; x++) {
            html += "<tr>";
            for (int i = 0; i < columns; i++) {
                String aux = getNodeFromGridPane(table, i, x).toString();
                html += "<td>";
                if (aux.substring(aux.indexOf("'") + 1, aux.lastIndexOf("'")).contains("*")) {
                    html += "<font color='#1EAA46'>" + aux.substring(aux.indexOf("'") + 1, aux.lastIndexOf("'"));
                } else {
                    html += aux.substring(aux.indexOf("'") + 1, aux.lastIndexOf("'"));
                }
                html += "</td>";
            }
            html += "</tr>";
        }
        html += "</table></body></html>";
        fileWriter("index.html", html);
    }

    public static void exportAsLatex(GridPane table, int columns, int rows) {

    }
}
