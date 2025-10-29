import requests
from django.core.cache import cache

ENTITY_CACHE_TIME = 86400
LABEL_CACHE_TIME = 604800

SPARQL_ENDPOINT = "https://query.wikidata.org/sparql"
WIKIDATA_API_URL = "https://www.wikidata.org/w/api.php"

WIKIDATA_USER_AGENT = 'ConnectTheDots/1.0 (https://github.com/repo/connectthedots)'

def get_wikidata_headers(include_accept=False):
    """
    Returns standardized headers for Wikidata API calls.
    
    Args:
        include_accept: If True, includes Accept header for SPARQL queries
    
    Returns:
        dict: Headers dictionary for Wikidata API requests
    """
    headers = {'User-Agent': WIKIDATA_USER_AGENT}
    if include_accept:
        headers['Accept'] = 'application/sparql-json'
    return headers

def get_property_labels(property_ids):
    """Fetch labels for multiple properties in a single API call"""
    if not property_ids:
        return {}
        
    ids_param = "|".join(property_ids)
    
    cache_key = f"wikidata_props_batch_{'_'.join(sorted(property_ids))}"
    cached_labels = cache.get(cache_key)
    if cached_labels:
        return cached_labels
    
    try:
        params = {
            "action": "wbgetentities",
            "ids": ids_param,
            "props": "labels",
            "languages": "en",
            "format": "json"
        }
        
        response = requests.get(WIKIDATA_API_URL, params=params, headers=get_wikidata_headers(), timeout=5)
        data = response.json()
        
        result = {}
        if 'entities' in data:
            for prop_id, entity_data in data['entities'].items():
                if 'labels' in entity_data and 'en' in entity_data['labels']:
                    result[prop_id] = entity_data['labels']['en']['value']
                else:
                    result[prop_id] = prop_id.replace('P', 'Property ')
        
        cache.set(cache_key, result, LABEL_CACHE_TIME)
        return result
    
    except Exception as e:
        print(f"Error fetching property labels: {str(e)}")
        return {prop_id: prop_id.replace('P', 'Property ') for prop_id in property_ids}

def format_property_value(value):
    """Format property value in a human-readable way and identify entity references"""
    if isinstance(value, dict):
        if 'entity-type' in value and 'id' in value:
            entity_id = value.get('id')
            entity_type = value.get('entity-type')
            return {
                'type': 'entity',
                'id': entity_id,
                'entity_type': entity_type,
                'text': entity_id
            }
        if 'time' in value:
            return value['time']
        return str(value)
    
    return str(value)

def execute_sparql_query(query):
    """Executes a SPARQL query against the Wikidata endpoint."""
    params = {'query': query, 'format': 'json'}
    try:
        response = requests.post(SPARQL_ENDPOINT, headers=get_wikidata_headers(include_accept=True), data=params, timeout=30)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"SPARQL query failed: {e}")
        return None

def get_wikidata_properties(entity_id):
    """
    Fetch properties for a Wikidata entity using a single SPARQL query.
    Returns property data with human-readable labels for properties and their values.
    """
    cache_key = f"wikidata_props_sparql_v2_{entity_id}"
    cached_props = cache.get(cache_key)
    if cached_props is not None:
        return cached_props

    query = f"""
    PREFIX wd: <http://www.wikidata.org/entity/>
    PREFIX wikibase: <http://wikiba.se/ontology#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX bd: <http://www.bigdata.com/rdf#>
    SELECT ?statement ?property ?propertyLabel ?value ?valueLabel WHERE {{
      BIND(wd:{entity_id} AS ?entity)
      ?entity ?p ?statement .
      ?property wikibase:claim ?p .
      ?statement ?ps ?value .
      ?property wikibase:statementProperty ?ps .
      FILTER(!isBLANK(?value))
      SERVICE wikibase:label {{ 
        bd:serviceParam wikibase:language "en" . 
        ?property rdfs:label ?propertyLabel .
        ?value rdfs:label ?valueLabel .
      }}
    }}
    """

    data = execute_sparql_query(query)
    
    if not data or 'results' not in data or 'bindings' not in data['results']:
        return []

    properties = []
    for item in data['results']['bindings']:
        prop_id = item.get('property', {}).get('value', '').split('/')[-1]
        statement_uri = item.get('statement', {}).get('value', '')
        statement_id = statement_uri.split('/')[-1] if statement_uri else None

        prop_label = item.get('propertyLabel', {}).get('value', prop_id)
        value_node = item.get('value', {})
        value_type = value_node.get('type')
        raw_value = value_node.get('value')
        display_value = item.get('valueLabel', {}).get('value', raw_value)

        value = display_value
        if value_type == 'uri':
            value = {'type': 'entity', 'id': raw_value.split('/')[-1], 'text': display_value}
        elif value_type == 'literal' and 'datatype' in value_node and 'xmls#dateTime' in value_node['datatype']:
            display_value = raw_value.split('T')[0]
            value = display_value
        
        properties.append({
            "statement_id": statement_id,
            "property": prop_id,
            "property_label": prop_label,
            "value": value,
            "display": f"{prop_label}: {display_value}"
        })

    cache.set(cache_key, properties, ENTITY_CACHE_TIME)
    return properties
