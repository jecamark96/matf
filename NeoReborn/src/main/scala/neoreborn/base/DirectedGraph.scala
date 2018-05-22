package neoreborn.base

class DirectedGraph(name : String, weighted : Boolean) extends Graph(name, weighted) {

  override def addEdge(idx_a: Int, idx_b: Int, weight: Int): Unit =
  {
    if (hasEdge(idx_a, idx_b)) return
    this(idx_a).addEdge(this(idx_b), weight)
    edgeCount.set(edgeCount.get + 1)
  }

  override def removeEdge(idx_a: Int, idx_b: Int): Unit =
  {
    if (!hasEdge(idx_a, idx_b)) return
    this(idx_a).removeEdge(this(idx_b))
    edgeCount.set(edgeCount.get - 1)
  }

  override def updateEdgeWeight(idx_a: Int, idx_b: Int, weight: Int): Unit =
  {
    if (!hasEdge(idx_a, idx_b)) return
    this(idx_a).updateWeight(this(idx_b), weight)
  }
}
