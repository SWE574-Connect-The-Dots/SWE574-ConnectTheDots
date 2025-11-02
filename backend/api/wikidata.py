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

def extract_location_from_properties(properties):
    """
    Extract location information from Wikidata properties.
    Returns a dictionary with location fields: country, city, district, street, latitude, longitude, location_name
    """
    location_data = {
        'country': None,
        'city': None,
        'district': None,
        'street': None,
        'latitude': None,
        'longitude': None,
        'location_name': None
    }
    
    # Property IDs for location-related data
    LOCATION_PROPERTIES = {
        'P625': 'coordinate_location',  # coordinate location
        'P17': 'country',              # country
        'P131': 'located_in',          # located in the administrative territorial entity
        'P276': 'location',            # location
        'P159': 'headquarters_location', # headquarters location
        'P740': 'location_of_formation', # location of formation
        'P19': 'place_of_birth',       # place of birth
        'P20': 'place_of_death',       # place of death
        'P551': 'residence',           # residence
        'P937': 'work_location',       # work location
    }
    
    for prop in properties:
        prop_id = prop.get('property')
        value = prop.get('value')
        display_value = prop.get('display', '')
        
        if not prop_id or not value:
            continue
            
        # Extract coordinates (P625)
        if prop_id == 'P625':
            # Coordinate format in Wikidata is typically "Point(longitude latitude)"
            coord_text = str(value)
            if 'Point(' in coord_text:
                try:
                    coords = coord_text.replace('Point(', '').replace(')', '').strip()
                    lon, lat = coords.split()
                    location_data['latitude'] = float(lat)
                    location_data['longitude'] = float(lon)
                except (ValueError, IndexError):
                    pass
        
        # Extract country (P17)
        elif prop_id == 'P17':
            if isinstance(value, dict) and value.get('type') == 'entity':
                location_data['country'] = value.get('text')
            else:
                location_data['country'] = str(value)
        
        # Extract other location properties
        elif prop_id in ['P131', 'P276', 'P159', 'P740', 'P19', 'P20', 'P551', 'P937']:
            location_text = None
            if isinstance(value, dict) and value.get('type') == 'entity':
                location_text = value.get('text')
            else:
                location_text = str(value)
            
            if location_text and not location_data['location_name']:
                location_data['location_name'] = location_text
                
            # Try to parse city/district from location text
            if location_text and ',' in location_text:
                parts = [part.strip() for part in location_text.split(',')]
                if len(parts) >= 2:
                    if not location_data['city']:
                        location_data['city'] = parts[0]
                    if not location_data['district'] and len(parts) > 2:
                        location_data['district'] = parts[1]
    
    # If we have coordinates but missing location info, try reverse geocoding
    if (location_data['latitude'] and location_data['longitude'] and 
        not any([location_data['country'], location_data['city'], location_data['location_name']])):
        reverse_geocoded = reverse_geocode_coordinates(location_data['latitude'], location_data['longitude'])
        if reverse_geocoded:
            for key, value in reverse_geocoded.items():
                if value and not location_data[key]:
                    location_data[key] = value
    
    return location_data

def reverse_geocode_coordinates(latitude, longitude):
    """
    Convert coordinates to address information using Nominatim reverse geocoding API.
    Returns a dictionary with location fields: country, city, district, street, location_name
    """
    import requests
    import time
    
    try:
        # Use Nominatim API for reverse geocoding
        url = "https://nominatim.openstreetmap.org/reverse"
        params = {
            'lat': latitude,
            'lon': longitude,
            'format': 'json',
            'addressdetails': 1,
            'zoom': 18,  # High detail level
            'accept-language': 'en'
        }
        headers = {
            'User-Agent': 'ConnectTheDots/1.0'
        }
        
        response = requests.get(url, params=params, headers=headers, timeout=10)
        time.sleep(1)  # Be respectful to the API
        
        if response.status_code == 200:
            data = response.json()
            if 'address' in data:
                address = data['address']
                
                location_info = {
                    'country': None,
                    'city': None,
                    'district': None,
                    'street': None,
                    'location_name': None
                }
                
                # Extract country
                location_info['country'] = (
                    address.get('country') or
                    address.get('country_code', '').upper()
                )
                
                # Extract city (try multiple possible fields)
                location_info['city'] = (
                    address.get('city') or
                    address.get('town') or
                    address.get('municipality') or
                    address.get('village') or
                    address.get('hamlet')
                )
                
                # Extract district/area
                location_info['district'] = (
                    address.get('suburb') or
                    address.get('district') or
                    address.get('neighbourhood') or
                    address.get('quarter') or
                    address.get('city_district') or
                    address.get('state_district')
                )
                
                # Extract street
                location_info['street'] = (
                    address.get('road') or
                    address.get('pedestrian') or
                    address.get('path')
                )
                
                # Create a readable location name from display_name
                if 'display_name' in data:
                    # Take first few parts of display_name for a concise location
                    parts = data['display_name'].split(',')[:3]
                    location_info['location_name'] = ', '.join(part.strip() for part in parts)
                
                return location_info
                
    except Exception as e:
        print(f"Reverse geocoding error for coordinates ({latitude}, {longitude}): {e}")
    
    return None

def forward_geocode_address(country=None, city=None, district=None, street=None):
    """
    Convert address information to coordinates using Nominatim geocoding API.
    Returns a dictionary with latitude, longitude, and formatted location_name
    """
    import requests
    import time
    
    # Build address string from available components
    address_parts = []
    if street:
        address_parts.append(street)
    if district:
        address_parts.append(district)
    if city:
        address_parts.append(city)
    if country:
        address_parts.append(country)
    
    if not address_parts:
        return None
    
    address_string = ", ".join(address_parts)
    
    try:
        # Use Nominatim API for forward geocoding
        url = "https://nominatim.openstreetmap.org/search"
        params = {
            'q': address_string,
            'format': 'json',
            'addressdetails': 1,
            'limit': 1,
            'accept-language': 'en'
        }
        headers = {
            'User-Agent': 'ConnectTheDots/1.0'
        }
        
        response = requests.get(url, params=params, headers=headers, timeout=10)
        time.sleep(1)  # Be respectful to the API
        
        if response.status_code == 200:
            data = response.json()
            if data and len(data) > 0:
                result = data[0]
                
                geocoded_info = {
                    'latitude': None,
                    'longitude': None,
                    'location_name': None
                }
                
                # Extract coordinates
                if 'lat' in result and 'lon' in result:
                    geocoded_info['latitude'] = float(result['lat'])
                    geocoded_info['longitude'] = float(result['lon'])
                
                # Extract formatted location name
                if 'display_name' in result:
                    # Take first few parts of display_name for a concise location
                    parts = result['display_name'].split(',')[:4]
                    geocoded_info['location_name'] = ', '.join(part.strip() for part in parts)
                
                return geocoded_info
                
    except Exception as e:
        print(f"Forward geocoding error for address '{address_string}': {e}")
    
    return None
