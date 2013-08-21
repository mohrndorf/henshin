/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.giraph.examples;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.giraph.aggregators.LongSumAggregator;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.master.DefaultMasterCompute;
import org.apache.hadoop.io.ByteWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;
import static org.apache.giraph.examples.HenshinUtil
  .ApplicationStack;
import static org.apache.giraph.examples.HenshinUtil
  .ApplicationStackAggregator;
import static org.apache.giraph.examples.HenshinUtil
  .Match;
import static org.apache.giraph.examples.HenshinUtil
  .VertexId;

/**
 * Generated implementation of the Henshin unit "TwoTimesTwo".
 */
@Algorithm(
    name = "TwoTimesTwo"
)
public class TwoTimesTwo extends
  BasicComputation<VertexId, ByteWritable, ByteWritable, Match> {

  /**
   * Name of the rule application count aggregator.
   */
  public static final String AGGREGATOR_RULE_APPLICATIONS = "ruleApps";

  /**
   * Name of the node generation aggregator.
   */
  public static final String AGGREGATOR_NODE_GENERATION = "nodeGen";

  /**
   * Name of the application stack aggregator.
   */
  public static final String AGGREGATOR_APPLICATION_STACK = "appStack";

  /**
   * Type constant for "Vertex".
   */
  public static final ByteWritable TYPE_VERTEX
    = new ByteWritable((byte) 0);

  /**
   * Type constant for "left".
   */
  public static final ByteWritable TYPE_VERTEX_LEFT
    = new ByteWritable((byte) 1);

  /**
   * Type constant for "conn".
   */
  public static final ByteWritable TYPE_VERTEX_CONN
    = new ByteWritable((byte) 2);

  /**
   * Type constant for "right".
   */
  public static final ByteWritable TYPE_VERTEX_RIGHT
    = new ByteWritable((byte) 3);

  /**
   * Type constant for "VertexContainer".
   */
  public static final ByteWritable TYPE_VERTEX_CONTAINER
    = new ByteWritable((byte) 4);

  /**
   * Type constant for "vertices".
   */
  public static final ByteWritable TYPE_VERTEX_CONTAINER_VERTICES
    = new ByteWritable((byte) 5);

  /**
   * Rule constant for "TwoTimesTwo".
   */
  public static final int RULE_TWO_TIMES_TWO = 0;

  /**
   * Logging support.
   */
  protected static final Logger LOG = Logger.getLogger(TwoTimesTwo.class);

  /*
   * (non-Javadoc)
   * @see org.apache.giraph.graph.Computation#compute(
   *        org.apache.giraph.graph.Vertex, java.lang.Iterable)
   */
  @Override
  public void compute(
      Vertex<VertexId, ByteWritable, ByteWritable> vertex,
      Iterable<Match> matches) throws IOException {
    ApplicationStack stack =
      getAggregatedValue(AGGREGATOR_APPLICATION_STACK);
    if (stack.getStackSize() == 0) {
      long ruleApps = ((LongWritable)
        getAggregatedValue(AGGREGATOR_RULE_APPLICATIONS)).get();
      if (ruleApps == 0) {
        vertex.voteToHalt();
      }
      return;
    }
    int rule = stack.getLastUnit();
    int microstep = stack.getLastMicrostep();
    switch (rule) {
    case RULE_TWO_TIMES_TWO:
      matchTwoTimesTwo(vertex, matches, microstep);
      break;
    default:
      throw new RuntimeException("Unknown rule: " + rule);
    }
  }

  /**
   * Match (and apply) the rule "TwoTimesTwo".
   * This takes 7 microsteps.
   * @param vertex The current vertex.
   * @param matches The current matches.
   * @param microstep Current microstep.
   */
  protected void matchTwoTimesTwo(
      Vertex<VertexId, ByteWritable, ByteWritable> vertex,
      Iterable<Match> matches, int microstep) throws IOException {

    LOG.info("Vertex " + vertex.getId() + " in superstep " + getSuperstep() +
      " matching rule TwoTimesTwo in microstep " + microstep);
    for (Match match : matches) {
      LOG.info("Vertex " + vertex.getId() + " in superstep " + getSuperstep() +
        " received (partial) match " + match);
    }
    Set<Match> appliedMatches = new HashSet<Match>();
    if (microstep == 0) {
      // Matching node "a":
      boolean ok = vertex.getValue().get() == TYPE_VERTEX.get();
      ok = ok && vertex.getNumEdges() >= 2;
      if (ok) {
        Match match = new Match().append(vertex.getId());
        // Send the match along all "conn"-edges:
        for (Edge<VertexId, ByteWritable> edge : vertex.getEdges()) {
          if (edge.getValue().get() ==
            TYPE_VERTEX_CONN.get()) {
            LOG.info("Vertex " + vertex.getId() +
              " sending (partial) match " + match +
              " forward to vertex " + edge.getTargetVertexId());
            sendMessage(edge.getTargetVertexId(), match);
          }
        }
      }
    } else if (microstep == 1) {
      // Matching node "x":
      boolean ok = vertex.getValue().get() == TYPE_VERTEX.get();
      if (ok) {
        for (Match match : matches) {
          match = match.append(vertex.getId());
          if (!match.isInjective()) {
            continue;
          }
          // Send the message back to matches of node "a":
          VertexId recipient = match.getVertexId(0);
          LOG.info("Vertex " + vertex.getId() +
            " sending (partial) match " + match +
            " back to vertex " + recipient);
          sendMessage(recipient, match);
        }
      }
    } else if (microstep == 2) {
      for (Match match : matches) {
        // Send the match along all "conn"-edges:
        for (Edge<VertexId, ByteWritable> edge : vertex.getEdges()) {
          if (edge.getValue().get() ==
            TYPE_VERTEX_CONN.get()) {
            LOG.info("Vertex " + vertex.getId() +
              " sending (partial) match " + match +
              " forward to vertex " + edge.getTargetVertexId());
            sendMessage(edge.getTargetVertexId(), match);
          }
        }
      }
    } else if (microstep == 3) {
      // Matching node "y":
      boolean ok = vertex.getValue().get() == TYPE_VERTEX.get();
      if (ok) {
        for (Match match : matches) {
          match = match.append(vertex.getId());
          if (!match.isInjective()) {
            continue;
          }
          if (vertex.getId().compareTo(match.getVertexId(1)) < 0) {
            continue;
          }
          // Send the message back to matches of node "x":
          VertexId recipient = match.getVertexId(1);
          LOG.info("Vertex " + vertex.getId() +
            " sending (partial) match " + match +
            " back to vertex " + recipient);
          sendMessage(recipient, match);
        }
      }
    } else if (microstep == 4) {
      // Matching node "b":
      boolean ok = vertex.getValue().get() == TYPE_VERTEX.get();
      ok = ok && vertex.getNumEdges() >= 2;
      if (ok) {
        Match match = new Match().append(vertex.getId());
        // Send the match along all "conn"-edges:
        for (Edge<VertexId, ByteWritable> edge : vertex.getEdges()) {
          if (edge.getValue().get() ==
            TYPE_VERTEX_CONN.get()) {
            LOG.info("Vertex " + vertex.getId() +
              " sending (partial) match " + match +
              " forward to vertex " + edge.getTargetVertexId());
            sendMessage(edge.getTargetVertexId(), match);
          }
        }
      }
      // Keep matches received at node "x":
      for (Match match : matches) {
        VertexId id = match.getVertexId(1);
        if (vertex.getId().equals(id)) {
          LOG.info("Vertex " + id + " in superstep " + getSuperstep() +
            " sending (partial) match " + match + " to myself");
          sendMessage(id, match);
        }
      }
    } else if (microstep == 5) {
      // Joining matches at node "x":
      LOG.info("Vertex " + vertex.getId() + " in superstep " + getSuperstep() +
        " joining matches of rule TwoTimesTwo");
      for (Match m1 : matches) {
        VertexId id1 = m1.getVertexId(1);
        if (vertex.getId().equals(id1)) {
          for (Match m2 : matches) {
            VertexId id2 = m2.getVertexId(1);
            if (!vertex.getId().equals(id2)) {
              Match m = m1.append(m2);
              if (!m.isInjective()) {
                continue;
              }
              // Send the message back to match of node "b":
              VertexId recipient = m.getVertexId(3);
              LOG.info("Vertex " + vertex.getId() +
                " sending (partial) match " + m +
                " back to vertex " + recipient);
              sendMessage(recipient, m);
            }
          }
        }
      }
    } else if (microstep == 6) {
      for (Match match : matches) {
        // Node "b": check for edge to match of "y" of type "conn":
        VertexId targetId = match.getVertexId(2);
        for (Edge<VertexId, ByteWritable> edge :
          vertex.getEdges()) {
          if (edge.getValue().get() ==
            TYPE_VERTEX_CONN.get() &&
            edge.getTargetVertexId().equals(targetId)) {
            applyTwoTimesTwo(vertex, match, appliedMatches);
          }
        }
      }
    } else {
      throw new RuntimeException("Illegal microstep for rule " +
        "TwoTimesTwo: " + microstep);
    }
  }

  /**
   * Apply the rule "TwoTimesTwo" to a given match.
   * @param vertex The base vertex.
   * @param match The match object.
   * @param appliedMatches Set of already applied matches.
   * @return true if the rule was applied.
   * @throws IOException On I/O errors.
   */
  protected boolean applyTwoTimesTwo(Vertex<VertexId, ByteWritable,
    ByteWritable> vertex, Match match, Set<Match> appliedMatches)
    throws IOException {
    VertexId cur0 = match.getVertexId(0);
    VertexId cur3 = match.getVertexId(3);
    match = match.remove(2);
    match = match.remove(1);
    if (!appliedMatches.add(match)) {
      return false;
    }
    LOG.info("Vertex " + vertex.getId() +
      " applying rule TwoTimesTwo with match " + match);
    VertexId new0 =
      deriveVertexId(vertex.getId(), appliedMatches.size(), 0);
    addVertexRequest(new0, TYPE_VERTEX_CONTAINER);
    VertexId src0 = new0;
    VertexId trg0 = cur0;
    Edge<VertexId, ByteWritable> edge0 =
      EdgeFactory.create(trg0, TYPE_VERTEX_CONTAINER_VERTICES);
    addEdgeRequest(src0, edge0);
    VertexId src1 = new0;
    VertexId trg1 = cur3;
    Edge<VertexId, ByteWritable> edge1 =
      EdgeFactory.create(trg1, TYPE_VERTEX_CONTAINER_VERTICES);
    addEdgeRequest(src1, edge1);
    aggregate(AGGREGATOR_RULE_APPLICATIONS, new LongWritable(1));
    return true;
  }

  /**
   * Derive a new vertex Id from an exiting one.
   * @param baseId The base vertex Id.
   * @param matchIndex The index of the match.
   * @param vertexIndex The index of the new vertex.
   * @return The derived vertex Id.
   */
  private VertexId deriveVertexId(VertexId baseId, int matchIndex,
    int vertexIndex) {
    long generation = ((LongWritable) getAggregatedValue(
        AGGREGATOR_NODE_GENERATION)).get();
    return baseId
      .append((byte) generation)
      .append((byte) matchIndex)
      .append((byte) vertexIndex);
  }

  /**
   * Master compute which registers and updates the required aggregators.
   */
  public static class MasterCompute extends DefaultMasterCompute {

    /**
     * Stack for storing unit success flags.
     */
    private final Deque<Boolean> unitSuccesses =
      new ArrayDeque<Boolean>();

    /**
     * Stack for storing the execution orders of independent units.
     */
    private final Deque<List<Integer>> unitOrders =
      new ArrayDeque<List<Integer>>();

    /*
     * (non-Javadoc)
     * @see org.apache.giraph.master.DefaultMasterCompute#compute()
     */
    @Override
    public void compute() {
      long ruleApps = ((LongWritable)
        getAggregatedValue(AGGREGATOR_RULE_APPLICATIONS)).get();
      if (getSuperstep() > 0) {
        LOG.info(ruleApps + " rule applications in superstep " +
          (getSuperstep() - 1));
      }
      if (ruleApps > 0) {
        long nodeGen = ((LongWritable)
          getAggregatedValue(AGGREGATOR_NODE_GENERATION)).get();
        setAggregatedValue(AGGREGATOR_NODE_GENERATION,
          new LongWritable(nodeGen + 1));
      }
      ApplicationStack stack;
      if (getSuperstep() == 0) {
        stack = new ApplicationStack();
        stack = stack.append(RULE_TWO_TIMES_TWO, 0);
      } else {
        stack = getAggregatedValue(AGGREGATOR_APPLICATION_STACK);
        stack = nextRuleStep(stack, ruleApps);
      }
      setAggregatedValue(AGGREGATOR_APPLICATION_STACK, stack);
    }

    /**
     * Compute the next rule application stack.
     * @param stack Current application stack.
     * @param ruleApps Number of rule applications in last superstep.
     * @return the new application stack.
     */
    private ApplicationStack nextRuleStep(
      ApplicationStack stack, long ruleApps) {
      while (stack.getStackSize() > 0) {
        int unit = stack.getLastUnit();
        int microstep = stack.getLastMicrostep();
        stack = stack.removeLast();
        switch (unit) {
        case RULE_TWO_TIMES_TWO:
          stack = processTwoTimesTwo(
            stack, microstep, ruleApps);
          break;
        default:
          throw new RuntimeException("Unknown unit " + unit);
        }
        if (stack.getStackSize() > 0) {
          unit = stack.getLastUnit();
          if (unit == RULE_TWO_TIMES_TWO) {
            break;
          }
        }
      }
      return stack;
    }

   /**
     * Process Rule "TwoTimesTwo".
     * @param stack Current application stack.
     * @param microstep Current microstep.
     * @param ruleApps Number of rule applications in last superstep.
     * @return the new application stack.
     */
    private ApplicationStack processTwoTimesTwo(
      ApplicationStack stack, int microstep, long ruleApps) {
      if (microstep < 6) {
        stack = stack.append(RULE_TWO_TIMES_TWO, microstep + 1);
      } else {
        unitSuccesses.push(ruleApps > 0);
      }
      return stack;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.giraph.master.DefaultMasterCompute#initialize()
     */
    @Override
    public void initialize() throws InstantiationException,
        IllegalAccessException {
      registerAggregator(AGGREGATOR_RULE_APPLICATIONS,
        LongSumAggregator.class);
      registerPersistentAggregator(AGGREGATOR_NODE_GENERATION,
        LongSumAggregator.class);
      registerPersistentAggregator(AGGREGATOR_APPLICATION_STACK,
        ApplicationStackAggregator.class);
    }

  }
}
