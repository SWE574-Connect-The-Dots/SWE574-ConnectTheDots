import networkx as nx
from .models import Node, Edge, GraphSnapshot
from django.core.serializers import serialize, deserialize
import json

class SpaceGraph:
    def __init__(self, space_id):
        self.space_id = space_id
        self.graph = nx.Graph()

    def load_from_db(self):
        nodes = Node.objects.filter(created_by__joined_spaces__id=self.space_id)
        edges = Edge.objects.filter(source__in=nodes, target__in=nodes)

        for node in nodes:
            self.graph.add_node(node.id, label=node.label, wikidata_id=node.wikidata_id)

        for edge in edges:
            self.graph.add_edge(edge.source.id, edge.target.id, relation=edge.relation_property)

    def add_node(self, label, wikidata_id, created_by):
        node = Node.objects.create(label=label, wikidata_id=wikidata_id, created_by=created_by)
        self.graph.add_node(node.id, label=label, wikidata_id=wikidata_id)
        return node

    def add_edge(self, source_id, target_id, relation_property):
        source = Node.objects.get(id=source_id)
        target = Node.objects.get(id=target_id)
        edge = Edge.objects.create(source=source, target=target, relation_property=relation_property)
        self.graph.add_edge(source_id, target_id, relation=relation_property)
        return edge

    def shortest_path(self, source_id, target_id):
        return nx.shortest_path(self.graph, source=source_id, target=target_id)

    def get_connected_components(self):
        return list(nx.connected_components(self.graph))
    
    def create_snapshot(self, user):
        nodes = Node.objects.filter(created_by__joined_spaces__id=self.space_id)
        edges = Edge.objects.filter(source__in=nodes, target__in=nodes)

        snapshot = {
            'nodes': json.loads(serialize('json', nodes)),
            'edges': json.loads(serialize('json', edges))
        }

        graph_snapshot = GraphSnapshot.objects.create(
            space_id=self.space_id,
            created_by=user,
            snapshot_data=snapshot
        )
        return graph_snapshot

    def revert_to_snapshot(self, snapshot_id):
        snapshot = GraphSnapshot.objects.get(id=snapshot_id, space_id=self.space_id)

        Node.objects.filter(created_by__joined_spaces__id=self.space_id).delete()

        for node_obj in deserialize('json', json.dumps(snapshot.snapshot_data['nodes'])):
            node_obj.save()

        for edge_obj in deserialize('json', json.dumps(snapshot.snapshot_data['edges'])):
            edge_obj.save()

        self.load_from_db()