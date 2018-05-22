package neoreborn.base

import scala.collection.mutable.TreeMap

class GVertex(idx : Int) extends Ordered[GVertex]{
  protected var index = idx
  protected var adjacentNodes : TreeMap[GVertex, Int] = new TreeMap[GVertex, Int]

  final def addEdge(v: GVertex) : Option[Int] = addEdge(v, 0)
  final def addEdge(v: GVertex, weight: Int) : Option[Int] = updateWeight(v, weight)
  final def updateWeight(v: GVertex, weight: Int) = adjacentNodes.put(v, weight)
  final def getAdjacentNodesBasic = adjacentNodes.keySet
  final def getAdjacentNodes = adjacentNodes
  final def getIndex = index
  final def setIndex(newIdx : Int) = index = newIdx
  def getEdgeWeight(v : GVertex) = adjacentNodes(v)
  def removeEdge(v: GVertex) = adjacentNodes.remove(v)
  def isAdj(v : GVertex) = adjacentNodes.contains(v)

  override def compare(that: GVertex): Int = getIndex - that.getIndex
}
