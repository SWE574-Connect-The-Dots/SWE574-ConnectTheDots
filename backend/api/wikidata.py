import requests

def get_wikidata_properties(entity_id):
    endpoint = f"https://www.wikidata.org/wiki/Special:EntityData/{entity_id}.json"
    res = requests.get(endpoint)
    res.raise_for_status()
    entity_data = res.json()['entities'][entity_id]['claims']
    properties = [{"property": prop, "value": str(entity_data[prop][0]['mainsnak']['datavalue']['value'])}
                  for prop in entity_data if 'datavalue' in entity_data[prop][0]['mainsnak']]
    return properties
