#!/usr/bin/env python3
"""
Grafana Dashboard Setup Script
Creates a PostgreSQL datasource and dashboard showing node growth over time
"""

import json
import requests
import time
import os
from datetime import datetime, timedelta
from dotenv import load_dotenv

# Load environment variables from backend/.env file
load_dotenv('../backend/.env')

# Grafana configuration (from .env file)
GRAFANA_URL = os.getenv("GRAFANA_URL", "http://localhost:3001")
GRAFANA_USERNAME = os.getenv("GRAFANA_USERNAME", "admin")
GRAFANA_PASSWORD = os.getenv("GRAFANA_PASSWORD", "admin")

# PostgreSQL configuration (from .env file)
POSTGRES_CONFIG = {
    "host": os.getenv("POSTGRES_HOST", "db"),
    "port": os.getenv("POSTGRES_PORT", "5432"),
    "database": os.getenv("POSTGRES_DB", "mydb"),
    "username": os.getenv("POSTGRES_USER", "myuser"),
    "password": os.getenv("POSTGRES_PASSWORD", "yy")
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

def create_node_growth_dashboard(session, datasource_uid):
    """Create dashboard showing node creation analytics over time"""
    
    # SQL query for daily total nodes (cumulative)
    daily_growth_query = """
    WITH daily_creations AS (
        SELECT 
            date_trunc('day', created_at) as day,
            count(*) as new_nodes_this_day
        FROM api_node 
        WHERE created_at IS NOT NULL
            AND $__timeFilter(created_at)
        GROUP BY date_trunc('day', created_at)
    )
    SELECT 
        day as time,
        SUM(new_nodes_this_day) OVER (ORDER BY day ROWS UNBOUNDED PRECEDING) as total_nodes
    FROM daily_creations
    ORDER BY time
    """
    
    # SQL query for weekly total nodes (cumulative)
    weekly_growth_query = """
    WITH weekly_creations AS (
        SELECT 
            date_trunc('week', created_at) as week,
            count(*) as new_nodes_this_week
        FROM api_node 
        WHERE created_at IS NOT NULL
            AND $__timeFilter(created_at)
        GROUP BY date_trunc('week', created_at)
    )
    SELECT 
        week as time,
        SUM(new_nodes_this_week) OVER (ORDER BY week ROWS UNBOUNDED PRECEDING) as total_nodes
    FROM weekly_creations
    ORDER BY time
    """
    
    # SQL query for monthly total nodes (cumulative) - one point per month
    monthly_growth_query = """
    WITH monthly_creations AS (
        SELECT 
            date_trunc('month', created_at) as month,
            count(*) as new_nodes_this_month
        FROM api_node 
        WHERE created_at IS NOT NULL
            AND $__timeFilter(created_at)
        GROUP BY date_trunc('month', created_at)
    )
    SELECT 
        month as time,
        SUM(new_nodes_this_month) OVER (ORDER BY month ROWS UNBOUNDED PRECEDING) as total_nodes
    FROM monthly_creations
    ORDER BY time
    """
    
    dashboard_config = {
        "dashboard": {
            "id": None,
            "title": "ConnectTheDots Node Analytics",
            "description": "Node creation trends and analytics dashboard",
            "tags": ["nodes", "growth", "analytics", "connectthedots"],
            "timezone": "browser",
            "panels": [
                {
                    "id": 1,
                    "title": "Daily Total Nodes",
                    "type": "timeseries",
                    "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "palette-classic"},
                            "custom": {
                                "axisLabel": "Total Nodes",
                                "axisPlacement": "auto",
                                "barAlignment": 0,
                                "drawStyle": "line",
                                "fillOpacity": 20,
                                "gradientMode": "none",
                                "hideFrom": {"tooltip": False, "viz": False, "legend": False},
                                "lineInterpolation": "linear",
                                "lineWidth": 2,
                                "pointSize": 5,
                                "scaleDistribution": {"type": "linear"},
                                "showPoints": "never",
                                "spanNulls": False,
                                "stacking": {"group": "A", "mode": "none"},
                                "thresholdsStyle": {"mode": "off"}
                            },
                            "mappings": [],
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "red", "value": None},
                                    {"color": "yellow", "value": 0},
                                    {"color": "green", "value": 1}
                                ]
                            },
                            "unit": "short",
                            "displayName": "Total Nodes"
                        },
                        "overrides": []
                    },
                    "options": {
                        "tooltip": {"mode": "single", "sort": "none"},
                        "legend": {"displayMode": "list", "placement": "bottom"},
                        "displayMode": "single"
                    },
                    "targets": [
                        {
                            "datasource": {"type": "grafana-postgresql-datasource", "uid": datasource_uid},
                            "format": "time_series",
                            "group": [],
                            "metricColumn": "none",
                            "rawQuery": True,
                            "rawSql": daily_growth_query,
                            "refId": "A",
                            "select": [
                                [{"params": ["total_nodes"], "type": "column"}]
                            ],
                            "timeColumn": "time",
                            "where": [{"name": "$__timeFilter", "params": [], "type": "macro"}]
                        }
                    ]
                },
                {
                    "id": 2,
                    "title": "Weekly Total Nodes",
                    "type": "timeseries",
                    "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "palette-classic"},
                            "custom": {
                                "axisLabel": "Total Nodes",
                                "axisPlacement": "auto",
                                "barAlignment": 0,
                                "drawStyle": "line",
                                "fillOpacity": 30,
                                "gradientMode": "opacity",
                                "hideFrom": {"tooltip": False, "viz": False, "legend": False},
                                "lineInterpolation": "smooth",
                                "lineWidth": 3,
                                "pointSize": 6,
                                "scaleDistribution": {"type": "linear"},
                                "showPoints": "always",
                                "spanNulls": False,
                                "stacking": {"group": "A", "mode": "none"},
                                "thresholdsStyle": {"mode": "off"}
                            },
                            "mappings": [],
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "green", "value": None}
                                ]
                            },
                            "unit": "short",
                            "displayName": "Total Nodes"
                        },
                        "overrides": []
                    },
                    "options": {
                        "tooltip": {"mode": "single", "sort": "none"},
                        "legend": {"displayMode": "list", "placement": "bottom"}
                    },
                    "targets": [
                        {
                            "datasource": {"type": "grafana-postgresql-datasource", "uid": datasource_uid},
                            "format": "time_series",
                            "group": [],
                            "metricColumn": "none",
                            "rawQuery": True,
                            "rawSql": weekly_growth_query,
                            "refId": "A",
                            "select": [
                                [{"params": ["total_nodes"], "type": "column"}]
                            ],
                            "timeColumn": "time",
                            "where": [{"name": "$__timeFilter", "params": [], "type": "macro"}]
                        }
                    ]
                },
                {
                    "id": 3,
                    "title": "Monthly Total Nodes",
                    "type": "timeseries",
                    "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "palette-classic"},
                            "custom": {
                                "axisLabel": "Total Nodes",
                                "axisPlacement": "auto",
                                "barAlignment": 0,
                                "drawStyle": "points",
                                "fillOpacity": 0,
                                "gradientMode": "none",
                                "hideFrom": {"tooltip": False, "viz": False, "legend": False},
                                "lineInterpolation": "stepAfter",
                                "lineWidth": 3,
                                "pointSize": 12,
                                "scaleDistribution": {"type": "linear"},
                                "showPoints": "always",
                                "spanNulls": True,
                                "stacking": {"group": "A", "mode": "none"},
                                "thresholdsStyle": {"mode": "off"}
                            },
                            "mappings": [],
                            "thresholds": {
                                "mode": "absolute",
                                "steps": [
                                    {"color": "green", "value": None},
                                    {"color": "yellow", "value": 5},
                                    {"color": "red", "value": 20}
                                ]
                            },
                            "unit": "short",
                            "displayName": "Total Nodes"
                        },
                        "overrides": []
                    },
                    "options": {
                        "tooltip": {"mode": "single", "sort": "none"},
                        "legend": {"displayMode": "list", "placement": "bottom"}
                    },
                    "targets": [
                        {
                            "datasource": {"type": "grafana-postgresql-datasource", "uid": datasource_uid},
                            "format": "time_series",
                            "group": [],
                            "metricColumn": "none",
                            "rawQuery": True,
                            "rawSql": monthly_growth_query,
                            "refId": "A",
                            "select": [
                                [{"params": ["total_nodes"], "type": "column"}]
                            ],
                            "timeColumn": "time",
                            "where": [{"name": "$__timeFilter", "params": [], "type": "macro"}]
                        }
                    ]
                }
            ],
            "time": {"from": "now-90d", "to": "now"},
            "timepicker": {},
            "templating": {"list": []},
            "annotations": {"list": []},
            "refresh": "5m",
            "schemaVersion": 30,
            "version": 1,
            "links": []
        },
        "overwrite": True
    }
    
    print("Creating user growth dashboard...")
    
    response = session.post(f"{GRAFANA_URL}/api/dashboards/db", 
                          data=json.dumps(dashboard_config))
    
    if response.status_code == 200:
        result = response.json()
        print(f"Successfully created dashboard: {result.get('url', 'Unknown URL')}")
        return result
    else:
        print(f"Failed to create dashboard. Status: {response.status_code}")
        print(f"Response: {response.text}")
        return None

def main():
    """Main function to set up Grafana dashboard"""
    
    print("=== Grafana Dashboard Setup for ConnectTheDots ===")
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
        dashboard = create_node_growth_dashboard(session, datasource['uid'])
        if not dashboard:
            print("Failed to create dashboard")
            return False
        
        print("\n=== Setup Complete! ===")
        print(f"Grafana Dashboard URL: {GRAFANA_URL}")
        print(f"Username: {GRAFANA_USERNAME}")
        print(f"Password: {GRAFANA_PASSWORD}")
        print("\nThe dashboard includes:")
        print("- Daily Total Nodes (cumulative line chart)")
        print("- Weekly Total Nodes (cumulative line chart)")
        print("- Monthly Total Nodes (cumulative line chart)")
        
        return True
        
    except Exception as e:
        print(f"Error during setup: {str(e)}")
        return False

if __name__ == "__main__":
    success = main()
    exit(0 if success else 1)