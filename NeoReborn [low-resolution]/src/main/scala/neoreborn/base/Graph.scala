package neoreborn.base

import javafx.beans.property.{IntegerProperty, SimpleIntegerProperty, SimpleStringProperty, StringProperty}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer, Queue}

abstract class Graph(name: String, weighted: Boolean = false) extends Object{
  //graph name
  protected var graphName : StringProperty = new SimpleStringProperty(name.toUpperCase)
  def graphNameProperty : StringProperty = graphName
  def getName = name

  //vertices
  protected var vertices: ArrayBuffer[GVertex] = new ArrayBuffer[GVertex]
  final def addVertex(node: GVertex) =
  {
    vertices += node
    vertexCount.set(getSize)
  }
  final def apply(idx: Int) = vertices(idx)
  final def removeVertex(idx : Int) = // O(max(|E|,|V|))
  {
    val vertex = this(idx)
    vertices.map(v => removeEdge(v.getIndex, idx))
    vertex.getAdjacentNodesBasic.map(v => removeEdge(idx, v.getIndex))
    vertices -= vertex
    vertices.filter(v => v.getIndex > idx).map(v => v.setIndex(v.getIndex - 1))
    vertexCountProperty.setValue(vertexCountProperty.get - 1)
  }
  final def getSize = vertices.size
  protected var vertexCount : IntegerProperty = new SimpleIntegerProperty(0)
  def vertexCountProperty : IntegerProperty = vertexCount

  //edges
  protected var edgeCount : IntegerProperty = new SimpleIntegerProperty(0)
  def addEdge(idx_a : Int, idx_b : Int, weight: Int = 0)
  def removeEdge(idx_a : Int, idx_b : Int)
  def updateEdgeWeight(idx_a : Int, idx_b : Int, weight : Int)
  final def getEdgeWeight(idx_a : Int, idx_b : Int) = this(idx_a).getEdgeWeight(this(idx_b))
  final def hasEdge(idx_a : Int, idx_b : Int) = this(idx_a).isAdj(this(idx_b))
  def edgeCountProperty : IntegerProperty = edgeCount
  final def getEdges : List[(Int, Int)] =
  {
    val res : ListBuffer[(Int, Int)] = new ListBuffer[(Int, Int)]
    vertices.map(v1 => v1.getAdjacentNodesBasic.map(v2 => if (v1.getIndex < v2.getIndex || isDirected) res += ((v1.getIndex, v2.getIndex))))
    return res.toList
  }
  final def getAdjacentEdges(idx : Int) = this(idx).getAdjacentNodesBasic.map(_.getIndex)

  //properties
  final def isDirected = this.isInstanceOf[DirectedGraph]
  final def isWeighted = weighted

  //algorithms
  final def dfs(setRep : (Int, Int, Int) => Boolean, idx : Int, parent : GVertex = null, res : ListBuffer[Int] = new ListBuffer[Int], vis : ArrayBuffer[Boolean] = ArrayBuffer(Array.fill(getSize){false} : _*)): List[Int] =         //O(|V|+|E|)
  {
    val vertex = this(idx)
    vis(vertex.getIndex) = true
    if (parent != null)
      if (!setRep(parent.getIndex, idx, 0))
        throw new InterruptedException
    res += vertex.getIndex
    vertex.getAdjacentNodesBasic.map(v => if ((parent == null || v.getIndex != parent.getIndex) && !vis(v.getIndex)) dfs(setRep, v.getIndex, vertex, res, vis))
    if (parent != null) {
      if (!setRep(idx, parent.getIndex, 1))
        throw new InterruptedException
    }
    else
      return res.toList
    return null
  }
  final def bfs(setRep: (Int, Int, Int) => Boolean, idx: Int) : List[Int] = // O(|V|+|E|)
  {
   val res: Queue[Int] = new Queue[Int]
   val vis: Array[Boolean] = Array.fill(getSize) {
     false
   }
   vis(idx) = true
   res += idx
   while (!res.isEmpty)
     {
       val x = res.dequeue
       if (!setRep(x, x, 2)) throw new InterruptedException
       this (x).getAdjacentNodesBasic.map(v => if (!vis(v.getIndex) && v.getIndex != x) {
         if (!setRep(x, v.getIndex, 0)) throw new InterruptedException
         res += v.getIndex
         vis(v.getIndex) = true
         if (!setRep(v.getIndex, x, 1)) throw new InterruptedException
       })
     }
   return res.toList
 }
  final def transitiveClosure(setRep : (Int, Int, Int) => Boolean) =      //O(V^3)
  {
    vertices.map(v => {
      setRep(v.getIndex, v.getIndex, 256)
      val reachable = dfs(setRep, v.getIndex)
      setRep(v.getIndex, v.getIndex, 255)
      reachable.map(x => setRep(v.getIndex, x, -1))
    })
  }
  final def dijkstra(setRep : (Int, Int, Int, Int, mutable.PriorityQueue[(Int, Int)]) => Boolean, idx : Int) =
  {
    val res : mutable.PriorityQueue[(Int, Int)] = mutable.PriorityQueue.empty[(Int, Int)](Ordering.by((_ : (Int, Int))._1).reverse)
    val dist : Array[Int] = Array.fill(getSize) {Integer.MAX_VALUE}
    dist(idx) = 0
    res += ((0, idx))
    while (!res.isEmpty)
      {
        val v = res.dequeue
        val vIdx = v._2
        if (!setRep(vIdx, vIdx, -1, v._1, res)) throw new InterruptedException
        if (dist(vIdx) == v._1) {
          if (!setRep(vIdx, vIdx, -2, v._1, res)) throw new InterruptedException
          val adj = this (vIdx).getAdjacentNodes
          adj.map(x => if (dist(x._1.getIndex) > v._1 + x._2) {
            dist(x._1.getIndex) = v._1 + x._2
            res += ((v._1 + x._2, x._1.getIndex))
            if (!setRep(vIdx, x._1.getIndex, 0, v._1, res)) throw new InterruptedException
            if (!setRep(x._1.getIndex, vIdx, 1, v._1 + x._2, res)) throw new InterruptedException
          })
        }
      }
    dist
  }
}
