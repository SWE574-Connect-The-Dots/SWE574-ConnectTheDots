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

    dashboard_config = {
        "dashboard": {
            "id": None,
            "title": "ConnectTheDots Total User Growth Analytics",
            "description": "Shows total users and daily change percentage",
            "tags": ["total users", "growth", "analytics", "connectthedots"],
            "timezone": "browser",
            "panels": [
                {
                    "id": 1,
                    "title": "",
                    "type": "stat",
                    "gridPos": {"h": 4, "w": 4, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "black"},
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
                        "colorMode": "none",
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
        print(f"Username: {GRAFANA_USERNAME}")
        print(f"Password: {GRAFANA_PASSWORD}")
        print("\nThe card includes:")
        print("- Total Users (numeric with percentage change over last day)")

        
        return True
        
    except Exception as e:
        print(f"Error during setup: {str(e)}")
        return False

if __name__ == "__main__":
    success = main()
    exit(0 if success else 1)