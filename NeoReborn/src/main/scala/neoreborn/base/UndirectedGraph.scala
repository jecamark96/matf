package neoreborn.base

class UndirectedGraph(name : String, weighted: Boolean) extends Graph(name, weighted) {

  override def addEdge(idx_a: Int, idx_b: Int, weight: Int = 0): Unit = {
    if (hasEdge(idx_a, idx_b)) return
    this (idx_a).addEdge(this (idx_b), weight)
    this (idx_b).addEdge(this (idx_a), weight)
    edgeCount.set(edgeCount.get + 1)
  }

  override def updateEdgeWeight(idx_a: Int, idx_b: Int, weight: Int): Unit = {
    if (!hasEdge(idx_a, idx_b)) return
    this (idx_a).updateWeight(this (idx_b), weight)
    this (idx_b).updateWeight(this (idx_a), weight)
  }

  override def removeEdge(idx_a: Int, idx_b: Int): Unit = {
    if (!hasEdge(idx_a, idx_b)) return
    this (idx_a).removeEdge(this (idx_b))
    this (idx_b).removeEdge(this (idx_a))
    edgeCount.set(edgeCount.get - 1)
  }
}
