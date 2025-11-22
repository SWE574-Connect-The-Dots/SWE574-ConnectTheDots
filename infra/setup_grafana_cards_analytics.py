#!/usr/bin/env python3
"""
Grafana User and Space Cards Setup Script
Creates separate cards for user metrics and space metrics with daily, weekly, and monthly variations
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

def create_user_cards_dashboard(session):
    """Create dashboard with user metric cards - daily, weekly, monthly"""
    
    # Combined daily user query
    daily_users_query = """
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
        0) AS daily_change,
        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('day', CURRENT_TIMESTAMP)) AS new_users_today
    FROM auth_user;
    """

    # Combined weekly user query
    weekly_users_query = """
    SELECT
        COUNT(*) AS total_users,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= NOW() - INTERVAL '7 days')::numeric /
                        NULLIF((SELECT COUNT(*) FROM auth_user WHERE date_joined >= NOW() - INTERVAL '14 days' AND date_joined < NOW() - INTERVAL '7 days'), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS weekly_change,
        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('week', CURRENT_TIMESTAMP)) AS new_users_this_week
    FROM auth_user;
    """

    # Combined monthly user query
    monthly_users_query = """
    SELECT
        COUNT(*) AS total_users,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('month', CURRENT_DATE))::numeric /
                        NULLIF((SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('month', CURRENT_DATE - INTERVAL '1 month') AND date_joined < date_trunc('month', CURRENT_DATE)), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS monthly_change,
        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('month', CURRENT_DATE)) AS new_users_this_month
    FROM auth_user;
    """

    dashboard_config = {
        "dashboard": {
            "id": None,
            "title": "ConnectTheDots User & Space Analytics",
            "description": "User and space metrics with daily, weekly, and monthly breakdowns - accessible design",
            "tags": ["users", "spaces", "analytics", "connectthedots", "accessible"],
            "timezone": "browser",
            "panels": [
                # Daily User Metrics
                {
                    "id": 1,
                    "title": "Daily User Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 0, "y": 0},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {
                                "displayMode": "basic",
                                "orientation": "horizontal"
                            },
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_users"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Users"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#1f77b4"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "daily_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "mode": "absolute",
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_users_today"},
                                "properties": [
                                    {"id": "displayName", "value": "New Today"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#17becf"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [
                        {
                            "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": daily_users_query,
                            "refId": "A"
                        }
                    ],
                    "transparent": False,
                    "background": "#ffffff"
                },
                # Weekly User Metrics
                {
                    "id": 2,
                    "title": "Weekly User Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 8, "y": 0},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {
                                "displayMode": "basic",
                                "orientation": "horizontal"
                            },
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_users"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Users"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#9467bd"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "weekly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "mode": "absolute",
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_users_this_week"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Week"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#bcbd22"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [
                        {
                            "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": weekly_users_query,
                            "refId": "A"
                        }
                    ],
                    "transparent": False,
                    "background": "#ffffff"
                },
                # Monthly User Metrics
                {
                    "id": 3,
                    "title": "Monthly User Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 16, "y": 0},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {
                                "displayMode": "basic",
                                "orientation": "horizontal"
                            },
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_users"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Users"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#e377c2"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "monthly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "mode": "absolute",
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_users_this_month"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Month"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#7f7f7f"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [
                        {
                            "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                            "format": "table",
                            "rawQuery": True,
                            "rawSql": monthly_users_query,
                            "refId": "A"
                        }
                    ],
                    "transparent": False,
                    "background": "#ffffff"
                }
            ],
            "time": {"from": "now-30d", "to": "now"},
            "refresh": "5m",
            "schemaVersion": 30,
            "version": 1
        },
        "overwrite": True
    }

def create_combined_analytics_dashboard(session):
    """Create combined dashboard with both user and space metrics"""
    
    # Combined daily user query
    daily_users_query = """
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
        0) AS daily_change,
        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('day', CURRENT_TIMESTAMP)) AS new_users_today
    FROM auth_user;
    """

    # Combined weekly user query
    weekly_users_query = """
    SELECT
        COUNT(*) AS total_users,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= NOW() - INTERVAL '7 days')::numeric /
                        NULLIF((SELECT COUNT(*) FROM auth_user WHERE date_joined >= NOW() - INTERVAL '14 days' AND date_joined < NOW() - INTERVAL '7 days'), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS weekly_change,
        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('week', CURRENT_TIMESTAMP)) AS new_users_this_week
    FROM auth_user;
    """

    # Combined monthly user query
    monthly_users_query = """
    SELECT
        COUNT(*) AS total_users,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('month', CURRENT_DATE))::numeric /
                        NULLIF((SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('month', CURRENT_DATE - INTERVAL '1 month') AND date_joined < date_trunc('month', CURRENT_DATE)), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS monthly_change,
        (SELECT COUNT(*) FROM auth_user WHERE date_joined >= date_trunc('month', CURRENT_DATE)) AS new_users_this_month
    FROM auth_user;
    """

    # Combined daily space query
    daily_spaces_query = """
    SELECT
        COUNT(*) AS total_spaces,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM api_space WHERE created_at >= NOW() - INTERVAL '1 day')::numeric /
                        NULLIF((SELECT COUNT(*) FROM api_space WHERE created_at >= NOW() - INTERVAL '2 days' AND created_at < NOW() - INTERVAL '1 day'), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS daily_change,
        (SELECT COUNT(*) FROM api_space WHERE created_at >= date_trunc('day', CURRENT_TIMESTAMP)) AS new_spaces_today
    FROM api_space;
    """

    # Combined weekly space query
    weekly_spaces_query = """
    SELECT
        COUNT(*) AS total_spaces,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM api_space WHERE created_at >= NOW() - INTERVAL '7 days')::numeric /
                        NULLIF((SELECT COUNT(*) FROM api_space WHERE created_at >= NOW() - INTERVAL '14 days' AND created_at < NOW() - INTERVAL '7 days'), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS weekly_change,
        (SELECT COUNT(*) FROM api_space WHERE created_at >= date_trunc('week', CURRENT_TIMESTAMP)) AS new_spaces_this_week
    FROM api_space;
    """

    # Combined monthly space query
    monthly_spaces_query = """
    SELECT
        COUNT(*) AS total_spaces,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM api_space WHERE created_at >= date_trunc('month', CURRENT_DATE))::numeric /
                        NULLIF((SELECT COUNT(*) FROM api_space WHERE created_at >= date_trunc('month', CURRENT_DATE - INTERVAL '1 month') AND created_at < date_trunc('month', CURRENT_DATE)), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS monthly_change,
        (SELECT COUNT(*) FROM api_space WHERE created_at >= date_trunc('month', CURRENT_DATE)) AS new_spaces_this_month
    FROM api_space;
    """

    # Combined daily node query
    daily_nodes_query = """
    SELECT
        COUNT(*) AS total_nodes,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM api_node WHERE created_at >= NOW() - INTERVAL '1 day')::numeric /
                        NULLIF((SELECT COUNT(*) FROM api_node WHERE created_at >= NOW() - INTERVAL '2 days' AND created_at < NOW() - INTERVAL '1 day'), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS daily_change,
        (SELECT COUNT(*) FROM api_node WHERE created_at >= date_trunc('day', CURRENT_TIMESTAMP)) AS new_nodes_today
    FROM api_node;
    """

    # Combined weekly node query
    weekly_nodes_query = """
    SELECT
        COUNT(*) AS total_nodes,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM api_node WHERE created_at >= NOW() - INTERVAL '7 days')::numeric /
                        NULLIF((SELECT COUNT(*) FROM api_node WHERE created_at >= NOW() - INTERVAL '14 days' AND created_at < NOW() - INTERVAL '7 days'), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS weekly_change,
        (SELECT COUNT(*) FROM api_node WHERE created_at >= date_trunc('week', CURRENT_TIMESTAMP)) AS new_nodes_this_week
    FROM api_node;
    """

    # Combined monthly node query
    monthly_nodes_query = """
    SELECT
        COUNT(*) AS total_nodes,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM api_node WHERE created_at >= date_trunc('month', CURRENT_DATE))::numeric /
                        NULLIF((SELECT COUNT(*) FROM api_node WHERE created_at >= date_trunc('month', CURRENT_DATE - INTERVAL '1 month') AND created_at < date_trunc('month', CURRENT_DATE)), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS monthly_change,
        (SELECT COUNT(*) FROM api_node WHERE created_at >= date_trunc('month', CURRENT_DATE)) AS new_nodes_this_month
    FROM api_node;
    """

    # Combined daily edge query
    daily_edges_query = """
    SELECT
        COUNT(*) AS total_edges,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM api_edge WHERE created_at >= NOW() - INTERVAL '1 day')::numeric /
                        NULLIF((SELECT COUNT(*) FROM api_edge WHERE created_at >= NOW() - INTERVAL '2 days' AND created_at < NOW() - INTERVAL '1 day'), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS daily_change,
        (SELECT COUNT(*) FROM api_edge WHERE created_at >= date_trunc('day', CURRENT_TIMESTAMP)) AS new_edges_today
    FROM api_edge;
    """

    # Combined weekly edge query
    weekly_edges_query = """
    SELECT
        COUNT(*) AS total_edges,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM api_edge WHERE created_at >= NOW() - INTERVAL '7 days')::numeric /
                        NULLIF((SELECT COUNT(*) FROM api_edge WHERE created_at >= NOW() - INTERVAL '14 days' AND created_at < NOW() - INTERVAL '7 days'), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS weekly_change,
        (SELECT COUNT(*) FROM api_edge WHERE created_at >= date_trunc('week', CURRENT_TIMESTAMP)) AS new_edges_this_week
    FROM api_edge;
    """

    # Combined monthly edge query
    monthly_edges_query = """
    SELECT
        COUNT(*) AS total_edges,
        COALESCE(
            ROUND(
                (
                    (
                        (SELECT COUNT(*) FROM api_edge WHERE created_at >= date_trunc('month', CURRENT_DATE))::numeric /
                        NULLIF((SELECT COUNT(*) FROM api_edge WHERE created_at >= date_trunc('month', CURRENT_DATE - INTERVAL '1 month') AND created_at < date_trunc('month', CURRENT_DATE)), 0)
                    ) - 1
                ) * 100, 2
            ),
        0) AS monthly_change,
        (SELECT COUNT(*) FROM api_edge WHERE created_at >= date_trunc('month', CURRENT_DATE)) AS new_edges_this_month
    FROM api_edge;
    """

    dashboard_config = {
        "dashboard": {
            "id": None,
            "title": "ConnectTheDots Analytics Dashboard",
            "description": "Combined user and space metrics with daily, weekly, and monthly breakdowns - accessible design",
            "tags": ["users", "spaces", "analytics", "connectthedots", "accessible"],
            "timezone": "browser",
            "panels": [
                # User Metrics Row
                {
                    "id": 1,
                    "title": "Daily User Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 0, "y": 0},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_users"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Users"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#1f77b4"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "daily_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_users_today"},
                                "properties": [
                                    {"id": "displayName", "value": "New Today"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#17becf"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": daily_users_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                {
                    "id": 2,
                    "title": "Weekly User Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 8, "y": 0},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_users"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Users"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#9467bd"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "weekly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_users_this_week"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Week"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#bcbd22"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": weekly_users_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                {
                    "id": 3,
                    "title": "Monthly User Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 16, "y": 0},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_users"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Users"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#e377c2"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "monthly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_users_this_month"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Month"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#7f7f7f"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": monthly_users_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                # Space Metrics Row
                {
                    "id": 4,
                    "title": "Daily Space Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 0, "y": 8},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_spaces"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Spaces"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#8c564b"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "daily_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_spaces_today"},
                                "properties": [
                                    {"id": "displayName", "value": "New Today"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#2ca02c"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": daily_spaces_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                {
                    "id": 5,
                    "title": "Weekly Space Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 8, "y": 8},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_spaces"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Spaces"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#ff7f0e"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "weekly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_spaces_this_week"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Week"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#98df8a"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": weekly_spaces_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                {
                    "id": 6,
                    "title": "Monthly Space Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 16, "y": 8},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_spaces"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Spaces"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#c5b0d5"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "monthly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_spaces_this_month"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Month"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#c49c94"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": monthly_spaces_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                # Node Metrics Row
                {
                    "id": 7,
                    "title": "Daily Node Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 0, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_nodes"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Nodes"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#2ca02c"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "daily_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_nodes_today"},
                                "properties": [
                                    {"id": "displayName", "value": "New Today"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#98df8a"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": daily_nodes_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                {
                    "id": 8,
                    "title": "Weekly Node Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 8, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_nodes"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Nodes"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#ff9f40"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "weekly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_nodes_this_week"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Week"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#ffcd56"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": weekly_nodes_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                {
                    "id": 9,
                    "title": "Monthly Node Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 16, "y": 16},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_nodes"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Nodes"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#54a24b"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "monthly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_nodes_this_month"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Month"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#88d27a"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": monthly_nodes_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                # Edge Metrics Row
                {
                    "id": 10,
                    "title": "Daily Edge Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 0, "y": 24},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_edges"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Edges"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#d62728"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "daily_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_edges_today"},
                                "properties": [
                                    {"id": "displayName", "value": "New Today"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#ff9f9b"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": daily_edges_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                {
                    "id": 11,
                    "title": "Weekly Edge Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 8, "y": 24},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_edges"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Edges"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#c44e52"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "weekly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_edges_this_week"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Week"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#de8f85"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": weekly_edges_query,
                        "refId": "A"
                    }],
                    "transparent": False
                },
                {
                    "id": 12,
                    "title": "Monthly Edge Metrics",
                    "type": "stat",
                    "gridPos": {"h": 8, "w": 8, "x": 16, "y": 24},
                    "fieldConfig": {
                        "defaults": {
                            "color": {"mode": "fixed", "fixedColor": "text"},
                            "custom": {"displayMode": "basic", "orientation": "horizontal"},
                            "mappings": [],
                            "unit": "short"
                        },
                        "overrides": [
                            {
                                "matcher": {"id": "byName", "options": "total_edges"},
                                "properties": [
                                    {"id": "displayName", "value": "Total Edges"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#b85450"}}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "monthly_change"},
                                "properties": [
                                    {"id": "displayName", "value": "Change (%)"},
                                    {"id": "unit", "value": "percent"},
                                    {"id": "color", "value": {"mode": "thresholds"}},
                                    {"id": "thresholds", "value": {
                                        "steps": [
                                            {"color": "#d62728", "value": None},
                                            {"color": "#ff7f0e", "value": 0},
                                            {"color": "#2ca02c", "value": 1}
                                        ]
                                    }}
                                ]
                            },
                            {
                                "matcher": {"id": "byName", "options": "new_edges_this_month"},
                                "properties": [
                                    {"id": "displayName", "value": "New This Month"},
                                    {"id": "color", "value": {"mode": "fixed", "fixedColor": "#ca8861"}}
                                ]
                            }
                        ]
                    },
                    "options": {
                        "orientation": "vertical",
                        "colorMode": "value",
                        "graphMode": "none",
                        "justifyMode": "center",
                        "reduceOptions": {"calcs": ["lastNotNull"]},
                        "textMode": "value_and_name"
                    },
                    "targets": [{
                        "datasource": {"type": "grafana-postgresql-datasource", "uid": "ef322m43946psb"},
                        "format": "table",
                        "rawQuery": True,
                        "rawSql": monthly_edges_query,
                        "refId": "A"
                    }],
                    "transparent": False
                }
            ],
            "time": {"from": "now-30d", "to": "now"},
            "refresh": "5m",
            "schemaVersion": 30,
            "version": 1
        },
        "overwrite": True
    }

    print("Creating combined analytics dashboard...")
    
    response = session.post(f"{GRAFANA_URL}/api/dashboards/db", 
                          data=json.dumps(dashboard_config))
    
    if response.status_code == 200:
        result = response.json()
        print(f"Successfully created combined analytics dashboard: {result.get('url', 'Unknown URL')}")
        return result
    else:
        print(f"Failed to create combined analytics dashboard. Status: {response.status_code}")
        print(f"Response: {response.text}")
        return None

def main():
    """Main function to set up Grafana user and space cards"""
    
    print("=== Grafana User & Space Cards Setup for ConnectTheDots ===")
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
        
        # Create combined analytics dashboard
        dashboard = create_combined_analytics_dashboard(session)
        if not dashboard:
            print("Failed to create combined analytics dashboard")
            return False
        
        print("\n=== Setup Complete! ===")
        print(f"Grafana Dashboard URL: {GRAFANA_URL}")
        print("\nCreated dashboard:")
        print("1. Combined Analytics Dashboard - User and Space metrics")
        print("\nDashboard includes:")
        print("USER METRICS:")
        print("- Daily: Total users, change %, new today")
        print("- Weekly: Total users, change %, new this week")
        print("- Monthly: Total users, change %, new this month")
        print("SPACE METRICS:")
        print("- Daily: Total spaces, change %, new today")
        print("- Weekly: Total spaces, change %, new this week")
        print("- Monthly: Total spaces, change %, new this month")
        print("\nAccessibility features:")
        print("- White background for better readability")
        print("- Colorblind-friendly color palette")
        print("- High contrast text")
        
        return True
        
    except Exception as e:
        print(f"Error during setup: {str(e)}")
        return False

if __name__ == "__main__":
    success = main()
    exit(0 if success else 1)