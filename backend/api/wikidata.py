import requests
from django.core.cache import cache

MAX_PROPERTIES = 50
ENTITY_CACHE_TIME = 86400
LABEL_CACHE_TIME = 604800

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
        url = "https://www.wikidata.org/w/api.php"
        params = {
            "action": "wbgetentities",
            "ids": ids_param,
            "props": "labels",
            "languages": "en",
            "format": "json"
        }
        
        response = requests.get(url, params=params, timeout=5)
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

def get_wikidata_properties(entity_id):
    """
    Fetch properties for a Wikidata entity with basic caching.
    Returns property data with human-readable labels.
    """
    cache_key = f"wikidata_props_{entity_id}"
    cached_props = cache.get(cache_key)
    
    if cached_props is not None:
        return cached_props
    
    try:
        endpoint = f"https://www.wikidata.org/wiki/Special:EntityData/{entity_id}.json"
        res = requests.get(endpoint, timeout=5)
        res.raise_for_status()
        
        entity_data = res.json()['entities'][entity_id]['claims']
        property_ids = list(entity_data.keys())[:MAX_PROPERTIES]
        
        property_labels = get_property_labels(property_ids)
        
        properties = []
        for prop in property_ids:
            if 'datavalue' in entity_data[prop][0]['mainsnak']:
                raw_value = entity_data[prop][0]['mainsnak']['datavalue']['value']
                formatted_value = format_property_value(raw_value)
                
                property_label = property_labels.get(prop, prop.replace('P', 'Property '))
                
                display_value = ""
                if isinstance(formatted_value, dict) and formatted_value.get('type') == 'entity':
                    display_value = formatted_value.get('text', '')
                else:
                    display_value = str(formatted_value)
                    
                properties.append({
                    "property": prop,
                    "property_label": property_label,
                    "value": formatted_value,
                    "display": f"{property_label}: {display_value}"
                })
        
        cache.set(cache_key, properties, ENTITY_CACHE_TIME)
        return properties
        
    except Exception as e:
        print(f"Error fetching properties for {entity_id}: {str(e)}")
        return []
