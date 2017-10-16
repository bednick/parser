package main.java.ru.old.computationalModel.tree;

import main.java.ru.old.computationalModel.file.CMFile;
import main.java.ru.old.computationalModel.line.CMLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Реализует дерево зависимостей на основе CMFile
 */
public class CMTree {
    String nameHead;
    ArrayList<CMTreeVertex> head;

    public CMTree(CMFile cmFile, String nameHead) {
        this.nameHead = nameHead;
        this.head = new ArrayList<>();
        Queue<CMTreeVertex> queue = new LinkedList<CMTreeVertex>(); // добавляются все вершины, к которым надо найти входящие строки
        for (CMLine line : cmFile.getForOut(nameHead)) {
            CMTreeVertex cmTreeVertex = new CMTreeVertex(line);
            head.add(cmTreeVertex);
            queue.add(cmTreeVertex);
        }
        //// TODO: 22.10.2016 сделать проверку на цикл при добавлении в дерево!

        HashMap<CMLine, CMTreeVertex> allVertex = new HashMap<CMLine, CMTreeVertex>();

        while (queue.size() > 0) {
            CMTreeVertex cmTreeVertex = queue.poll();
            for (String nameIn : cmTreeVertex.getCmLine().getIn()) {
                for (CMLine line : cmFile.getForOut(nameIn)) {
                    if (!allVertex.containsKey(line)) {
                        allVertex.put(line, new CMTreeVertex(line)); //доделать (протестить, что корректно работает)
                    }
                    cmTreeVertex.getIn(nameIn).add(allVertex.get(line));
                    queue.add(allVertex.get(line));
                }
            }
        }
    }

    public ArrayList<CMTreeVertex> getHead() {
        return head;
    }

    @Override
    public String toString() {
        ArrayList<CMTreeVertex> allVertex = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        Queue<CMTreeVertex> queue = new LinkedList<CMTreeVertex>();

        stringBuilder.append("CMTree nameHead=");
        stringBuilder.append(nameHead);
        stringBuilder.append("\n");
        for (CMTreeVertex vertex : head) {
            queue.add(vertex);
            allVertex.add(vertex);
            stringBuilder.append(vertex);
            stringBuilder.append("\t\tisCanPerform=");
            stringBuilder.append(vertex.getCmLine().getFlags().isCanPerform());
            stringBuilder.append(" isStart=");
            stringBuilder.append(vertex.getCmLine().getFlags().isStart());
            stringBuilder.append("\n");
        }

        while (queue.size() > 0) {
            CMTreeVertex cmTreeVertex = queue.poll();
            for (String nameIn : cmTreeVertex.getCmLine().getIn()) {
                for (CMTreeVertex vertex : cmTreeVertex.getIn(nameIn)) {
                    if (!allVertex.contains(vertex)) {
                        queue.add(vertex);
                        allVertex.add(vertex);
                        stringBuilder.append(vertex);
                        stringBuilder.append("\t\tisCanPerform=");
                        stringBuilder.append(vertex.getCmLine().getFlags().isCanPerform());
                        stringBuilder.append(" isStart=");
                        stringBuilder.append(vertex.getCmLine().getFlags().isStart());
                        stringBuilder.append("\n");
                    }
                }
            }
        }
        stringBuilder.append("*\t*\t*\t*");

        stringBuilder.append("*\t*\t*\t*");
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
}
