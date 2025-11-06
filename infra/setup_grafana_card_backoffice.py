#!/usr/bin/env python3
"""
Grafana Dashboard Setup Script
Creates a PostgreSQL datasource and dashboard showing user growth over time
"""

import json
import requests
import time
import os
from datetime import datetime, timedelta

from sympy import false

# Grafana configuration
GRAFANA_URL = "http://localhost:3001"
GRAFANA_USERNAME = "admin"
GRAFANA_PASSWORD = "admin"

# PostgreSQL configuration (from docker-compose setup)
POSTGRES_CONFIG = {
    "host": "db",
    "port": "5432",
    "database": "mydb",
    "username": "myuser",
    "password": "yy"
}

def wait_for_grafana():
    """Wait for Grafana to be ready"""
    print("Waiting for Grafana to be ready...")
    max_retries = 30
    for i in range(max_retries):
        try:
            response = requests.get(f"{GRAFANA_URL}/api/health")
            if response.status_code == 200:
                print("Grafana is ready!")
                return True
        except requests.exceptions.ConnectionError:
            pass
        
        print(f"Attempt {i+1}/{max_retries}: Grafana not ready, waiting...")
        time.sleep(2)
    
    print("Grafana failed to start within expected time")
    return False

def create_session():
    """Create authenticated session with Grafana"""
    session = requests.Session()
    session.auth = (GRAFANA_USERNAME, GRAFANA_PASSWORD)
    session.headers.update({
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    })
    return session

def create_postgresql_datasource(session):
    """Create PostgreSQL datasource in Grafana"""
    
    datasource_config = {
        "name": "postgres",
        "type": "grafana-postgresql-datasource",
        "url": f"{POSTGRES_CONFIG['host']}:{POSTGRES_CONFIG['port']}",
        "access": "proxy",
        "database": POSTGRES_CONFIG['database'],
        "user": POSTGRES_CONFIG['username'],
        "secureJsonData": {
            "password": POSTGRES_CONFIG['password']
        },
        "jsonData": {
            "sslmode": "disable",
            "postgresVersion": 1600,
            "timescaledb": False,
            "maxOpenConns": 100,
            "maxIdleConns": 100,
            "connMaxLifetime": 14400
        },
        "isDefault": False
    }
    
    print("Creating PostgreSQL datasource...")
    
    # Check if datasource already exists
    response = session.get(f"{GRAFANA_URL}/api/datasources/name/{datasource_config['name']}")
    if response.status_code == 200:
        print(f"Datasource '{datasource_config['name']}' already exists")
        return response.json()
    
    # Create new datasource
    response = session.post(f"{GRAFANA_URL}/api/datasources", 
                          data=json.dumps(datasource_config))
    
    if response.status_code == 200:
        print(f"Successfully created datasource: {datasource_config['name']}")
        return response.json()
    else:
        print(f"Failed to create datasource. Status: {response.status_code}")
        print(f"Response: {response.text}")
        return None

def total_user_card(session):
    """Create Grafana dashboard showing total users and daily change percentage"""
    
    total_user_query = """
    SELECT
        COUNT(*) AS total_users,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= NOW() - INTERVAL '1 day')::numeric /
                        NULLIF((SELECT COUNT(*) FROM auth_user WHERE date_joined >= NOW() - INTERVAL '2 days' AND date_joined < NOW() - INTERVAL '1 day'), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS daily_change
    FROM auth_user;
    """

    total_user_montly_query = """
    SELECT 
    COUNT(*) AS new_users_this_month
    FROM auth_user
    WHERE date_joined >= date_trunc('month', CURRENT_DATE);
    """

    total_space_query = """
    SELECT COUNT(*) AS total_spaces
    FROM api_space;
    """

    total_new_spaces_montly_query = """
    SELECT 
        COUNT(*) AS new_spaces_this_month
    FROM api_space
    WHERE created_at >= date_trunc('month', CURRENT_DATE);
    """

    total_node_query = """
    SELECT COUNT(*) AS total_nodes
    FROM api_node;
    """

    total_new_nodes_montly_query = """
    SELECT 
        COUNT(*) AS new_nodes_this_month
    FROM api_node
    WHERE created_at >= date_trunc('month', CURRENT_DATE);
    """

    total_edge_query = """
    SELECT COUNT(*) AS total_edges
    FROM api_edge;
    """

    total_new_edges_montly_query = """
    SELECT 
        COUNT(*) AS new_edges_this_month
    FROM api_edge
    WHERE created_at >= date_trunc('month', CURRENT_DATE);
    """

    dashboard_config = {
        "dashboard": {
            "id": None,
            "title": "ConnectTheDots Back Office Numerics Analytics",
            "description": "Shows total users, spaces, nodes, edges and their growth over time.",
            "tags": ["growth", "analytics", "connectthedots", "backoffice"],
            "timezone": "browser",
            "panels": [
                {
                    "id": 1,
                    "title": "",
                    "type": "stat",
                    "gridPos": {"h": 4, "w": 4, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "thresholds"},
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "red", "value": None},     # decreased
                                    {"color": "yellow", "value": 0},     # stayed the same
                                    {"color": "green", "value": 1}       # increased
                                ]
                            },
                            "mappings": [],
                            "unit": "short",
                            "custom": {
                                "text": {"valueSize": 16},
                                "align": "center"
                            }
                        },
                        "overrides": []
                    },
                    "options": {
                        "orientation": "horizontal",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [
                        {
                            "datasource": {
                                "type": "grafana-postgresql-datasource",
                                "uid": "ef322m43946psb"
                            },
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": total_user_query,
                            "refId": "A"
                        }
                    ],
                    "transformations": [
                        {
                            "id": "organize",
                            "options": {
                                "renameByName": {
                                    "total_users": "Total Users",
                                    "daily_change": "Daily Change (%)"
                                }
                            }
                        }
                    ]
                },
                {
                    "id": 2,
                    "title": "New Users This Month",
                    "type": "stat",
                    "gridPos": {"h": 4, "w": 4, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "thresholds"},
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "red", "value": None},     # decreased
                                    {"color": "yellow", "value": 0},     # stayed the same
                                    {"color": "green", "value": 1}       # increased
                                ]
                            },
                            "mappings": [],
                            "unit": "short",
                            "custom": {
                                "text": {"valueSize": 16},
                                "align": "center"
                            }
                        },
                        "overrides": [
                            {
                            "matcher": {
                                "id": "byName",
                                "options": "new_users_this_month"
                            },
                            "properties": [
                                {
                                "id": "color",
                                "value": { "mode": "fixed", "fixedColor": "green" }
                                }
                            ]
                        }
                        ]
                    },
                    "options": {
                        "orientation": "horizontal",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value"
                    },
                    "targets": [
                        {
                            "datasource": {
                                "type": "grafana-postgresql-datasource",
                                "uid": "ef322m43946psb"
                            },
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": total_user_montly_query,
                            "refId": "A"
                        }
                    ]
                },
                {
                    "id": 3,
                    "title": "Total Spaces",
                    "type": "stat",
                    "gridPos": {"h": 4, "w": 4, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "thresholds"},
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "red", "value": None},     # decreased
                                    {"color": "yellow", "value": 0},     # stayed the same
                                    {"color": "green", "value": 1}       # increased
                                ]
                            },
                            "mappings": [],
                            "unit": "short",
                            "custom": {
                                "text": {"valueSize": 16},
                                "align": "center"
                            }
                        },
                        "overrides": [
                            {
                            "matcher": {
                                "id": "byName",
                                "options": "total_spaces"
                            },
                            "properties": [
                                {
                                "id": "color",
                                "value": { "mode": "fixed", "fixedColor": "yellow" }
                                }
                            ]
                        }
                        ]
                    },
                    "options": {
                        "orientation": "horizontal",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value"
                    },
                    "targets": [
                        {
                            "datasource": {
                                "type": "grafana-postgresql-datasource",
                                "uid": "ef322m43946psb"
                            },
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": total_space_query,
                            "refId": "A"
                        }
                    ]
                },
                {
                    "id": 4,
                    "title": "New Spaces This Month",
                    "type": "stat",
                    "gridPos": {"h": 4, "w": 4, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "thresholds"},
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "red", "value": None},     # decreased
                                    {"color": "yellow", "value": 0},     # stayed the same
                                    {"color": "green", "value": 1}       # increased
                                ]
                            },
                            "mappings": [],
                            "unit": "short",
                            "custom": {
                                "text": {"valueSize": 16},
                                "align": "center"
                            }
                        },
                        "overrides": [
                            {
                            "matcher": {
                                "id": "byName",
                                "options": "new_spaces_this_month"
                            },
                            "properties": [
                                {
                                "id": "color",
                                "value": { "mode": "fixed", "fixedColor": "green" }
                                }
                            ]
                        }
                        ]
                    },
                    "options": {
                        "orientation": "horizontal",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value"
                    },
                    "targets": [
                        {
                            "datasource": {
                                "type": "grafana-postgresql-datasource",
                                "uid": "ef322m43946psb"
                            },
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": total_new_spaces_montly_query,
                            "refId": "A"
                        }
                    ]
                },
                {
                    "id": 5,
                    "title": "Total Nodes",
                    "type": "stat",
                    "gridPos": {"h": 4, "w": 4, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "thresholds"},
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "red", "value": None},     # decreased
                                    {"color": "yellow", "value": 0},     # stayed the same
                                    {"color": "green", "value": 1}       # increased
                                ]
                            },
                            "mappings": [],
                            "unit": "short",
                            "custom": {
                                "text": {"valueSize": 16},
                                "align": "center"
                            }
                        },
                        "overrides": [
                            {
                            "matcher": {
                                "id": "byName",
                                "options": "total_nodes"
                            },
                            "properties": [
                                {
                                "id": "color",
                                "value": { "mode": "fixed", "fixedColor": "yellow" }
                                }
                            ]
                        }
                        ]
                    },
                    "options": {
                        "orientation": "horizontal",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value"
                    },
                    "targets": [
                        {
                            "datasource": {
                                "type": "grafana-postgresql-datasource",
                                "uid": "ef322m43946psb"
                            },
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": total_node_query,
                            "refId": "A"
                        }
                    ]
                },
                {
                    "id": 6,
                    "title": "New Nodes This Month",
                    "type": "stat",
                    "gridPos": {"h": 4, "w": 4, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "thresholds"},
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "red", "value": None},     # decreased
                                    {"color": "yellow", "value": 0},     # stayed the same
                                    {"color": "green", "value": 1}       # increased
                                ]
                            },
                            "mappings": [],
                            "unit": "short",
                            "custom": {
                                "text": {"valueSize": 16},
                                "align": "center"
                            }
                        },
                        "overrides": [
                            {
                            "matcher": {
                                "id": "byName",
                                "options": "new_nodes_this_month"
                            },
                            "properties": [
                                {
                                "id": "color",
                                "value": { "mode": "fixed", "fixedColor": "green" }
                                }
                            ]
                        }
                        ]
                    },
                    "options": {
                        "orientation": "horizontal",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value"
                    },
                    "targets": [
                        {
                            "datasource": {
                                "type": "grafana-postgresql-datasource",
                                "uid": "ef322m43946psb"
                            },
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": total_new_nodes_montly_query,
                            "refId": "A"
                        }
                    ]
                },
                {
                    "id": 7,
                    "title": "Total Edges",
                    "type": "stat",
                    "gridPos": {"h": 4, "w": 4, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "thresholds"},
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "red", "value": None},     # decreased
                                    {"color": "yellow", "value": 0},     # stayed the same
                                    {"color": "green", "value": 1}       # increased
                                ]
                            },
                            "mappings": [],
                            "unit": "short",
                            "custom": {
                                "text": {"valueSize": 16},
                                "align": "center"
                            }
                        },
                        "overrides": [
                            {
                            "matcher": {
                                "id": "byName",
                                "options": "total_edges"
                            },
                            "properties": [
                                {
                                "id": "color",
                                "value": { "mode": "fixed", "fixedColor": "yellow" }
                                }
                            ]
                        }
                        ]
                    },
                    "options": {
                        "orientation": "horizontal",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value"
                    },
                    "targets": [
                        {
                            "datasource": {
                                "type": "grafana-postgresql-datasource",
                                "uid": "ef322m43946psb"
                            },
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": total_edge_query,
                            "refId": "A"
                        }
                    ]
                },
                {
                    "id": 8,
                    "title": "New Edges This Month",
                    "type": "stat",
                    "gridPos": {"h": 4, "w": 4, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "thresholds"},
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "red", "value": None},     # decreased
                                    {"color": "yellow", "value": 0},     # stayed the same
                                    {"color": "green", "value": 1}       # increased
                                ]
                            },
                            "mappings": [],
                            "unit": "short",
                            "custom": {
                                "text": {"valueSize": 16},
                                "align": "center"
                            }
                        },
                        "overrides": [
                            {
                            "matcher": {
                                "id": "byName",
                                "options": "new_edges_this_month"
                            },
                            "properties": [
                                {
                                "id": "color",
                                "value": { "mode": "fixed", "fixedColor": "green" }
                                }
                            ]
                        }
                        ]
                    },
                    "options": {
                        "orientation": "horizontal",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value"
                    },
                    "targets": [
                        {
                            "datasource": {
                                "type": "grafana-postgresql-datasource",
                                "uid": "ef322m43946psb"
                            },
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": total_new_edges_montly_query,
                            "refId": "A"
                        }
                    ]
                }
            ],
            "time": {"from": "now-30d", "to": "now"},
            "refresh": "5m",
            "schemaVersion": 30,
            "version": 1
        },
        "overwrite": True
    }

    print("Creating total user card...")

    
    response = session.post(f"{GRAFANA_URL}/api/dashboards/db", 
                          data=json.dumps(dashboard_config))
    
    if response.status_code == 200:
        result = response.json()
        print(f"Successfully created card: {result.get('url', 'Unknown URL')}")
        return result
    else:
        print(f"Failed to create card. Status: {response.status_code}")
        print(f"Response: {response.text}")
        return None

def main():
    """Main function to set up Grafana card"""
    
    print("=== Grafana Card Setup for ConnectTheDots ===")
    print(f"Grafana URL: {GRAFANA_URL}")
    print(f"Database: {POSTGRES_CONFIG['database']}@{POSTGRES_CONFIG['host']}:{POSTGRES_CONFIG['port']}")
    print()
    
    # Wait for Grafana to be ready
    if not wait_for_grafana():
        print("Failed to connect to Grafana. Make sure it's running.")
        return False
    
    # Create session
    session = create_session()
    
    try:
        # Test Grafana connection
        response = session.get(f"{GRAFANA_URL}/api/org")
        if response.status_code != 200:
            print(f"Failed to authenticate with Grafana: {response.status_code}")
            return False
        
        print(f"Successfully authenticated with Grafana")
        
        # Create PostgreSQL datasource
        datasource = create_postgresql_datasource(session)
        if not datasource:
            print("Failed to create datasource")
            return False
        
        # Wait a moment for datasource to be ready
        time.sleep(2)
        
        # Create dashboard
        dashboard = total_user_card(session)
        if not dashboard:
            print("Failed to create dashboard")
            return False
        
        print("\n=== Setup Complete! ===")
        print(f"Grafana Dashboard URL: {GRAFANA_URL}")
        print("\nThe card includes:")
        print("- Total Users (numeric with percentage change over last day)")
        print("- New Users This Month")
        print("- Total Spaces")
        print("- New Spaces This Month")
        print("- Total Nodes")
        print("- New Nodes This Month")
        print("- Total Edges")
        print("- New Edges This Month")
        
        return True
        
    except Exception as e:
        print(f"Error during setup: {str(e)}")
        return False

if __name__ == "__main__":
    success = main()
    exit(0 if success else 1)