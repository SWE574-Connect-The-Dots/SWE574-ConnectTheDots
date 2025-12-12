"""
Django management command to fetch missing P31 (instance of) properties
for existing nodes that have Wikidata IDs but no P31 properties.

This is a one-time migration command that can be run safely multiple times.
It will only fetch P31 for nodes that don't already have it.

Usage:
    python manage.py fetch_missing_p31
    python manage.py fetch_missing_p31 --dry-run
    python manage.py fetch_missing_p31 --limit 100
"""

from django.core.management.base import BaseCommand
from api.models import Node, Property
from api.wikidata import get_wikidata_properties
from api.views import _normalize_property_value_for_storage
import time


class Command(BaseCommand):
    help = 'Fetch missing P31 (instance of) properties for nodes with Wikidata IDs'

    def add_arguments(self, parser):
        parser.add_argument(
            '--dry-run',
            action='store_true',
            help='Show what would be done without making changes',
        )
        parser.add_argument(
            '--limit',
            type=int,
            default=None,
            help='Limit the number of nodes to process',
        )
        parser.add_argument(
            '--delay',
            type=float,
            default=1.0,
            help='Delay in seconds between Wikidata API calls (default: 1.0)',
        )

    def handle(self, *args, **options):
        dry_run = options['dry_run']
        limit = options['limit']
        delay = options['delay']
        
        if dry_run:
            self.stdout.write(self.style.WARNING('DRY RUN MODE - No changes will be made'))
        
        nodes_with_wikidata = Node.objects.filter(
            wikidata_id__isnull=False
        ).exclude(wikidata_id='')
        
        nodes_without_p31 = []
        nodes_with_broken_p31 = []
        
        for node in nodes_with_wikidata:
            p31_props = Property.objects.filter(
                node=node,
                property_id='P31'
            )
            
            has_p31 = p31_props.exists()
            has_valid_p31 = p31_props.filter(value_id__isnull=False).exclude(value_id='').exists()
            
            if not has_p31:
                nodes_without_p31.append(node)
            elif not has_valid_p31:
                nodes_with_broken_p31.append(node)
            
            total_needs_fix = len(nodes_without_p31) + len(nodes_with_broken_p31)
            if limit and total_needs_fix >= limit:
                break
        
        total_nodes = len(nodes_without_p31) + len(nodes_with_broken_p31)
        
        if total_nodes == 0:
            self.stdout.write(self.style.SUCCESS('✓ All nodes with Wikidata IDs already have valid P31 properties!'))
            return
        
        self.stdout.write(f'Found {len(nodes_without_p31)} nodes without P31 properties')
        if nodes_with_broken_p31:
            self.stdout.write(f'Found {len(nodes_with_broken_p31)} nodes with P31 properties but missing value data')
        
        if dry_run:
            self.stdout.write('Would process the following nodes:')
            for node in (nodes_without_p31 + nodes_with_broken_p31)[:10]:
                node_type = 'missing P31' if node in nodes_without_p31 else 'broken P31'
                self.stdout.write(f'  - {node.label} ({node.wikidata_id}) [{node_type}]')
            if total_nodes > 10:
                self.stdout.write(f'  ... and {total_nodes - 10} more')
            return
        
        all_nodes_to_fix = nodes_without_p31 + nodes_with_broken_p31
        
        success_count = 0
        error_count = 0
        p31_found_count = 0
        p31_not_found_count = 0
        p31_fixed_count = 0
        
        for i, node in enumerate(all_nodes_to_fix, 1):
            try:
                self.stdout.write(f'[{i}/{total_nodes}] Processing: {node.label} ({node.wikidata_id})...')
                
                all_properties = get_wikidata_properties(node.wikidata_id)
                
                if not all_properties:
                    self.stdout.write(
                        self.style.WARNING(f'  ⚠ No properties returned from Wikidata (entity may not exist or query failed)')
                    )
                    success_count += 1
                    p31_not_found_count += 1
                    if i < total_nodes:
                        time.sleep(delay)
                    continue
                
                p31_props = [p for p in all_properties if p.get('property') == 'P31']
                
                if p31_props:
                    existing_p31 = Property.objects.filter(node=node, property_id='P31')
                    is_fixing_broken = existing_p31.exists() and not existing_p31.filter(value_id__isnull=False).exclude(value_id='').exists()
                    
                    if is_fixing_broken:
                        fixed_count = 0
                        for existing_prop in existing_p31:
                            matching_p31 = next((p for p in p31_props if p.get('statement_id') == existing_prop.statement_id), p31_props[0])
                            value_text, value_id = _normalize_property_value_for_storage(matching_p31.get('value'))
                            
                            existing_prop.value = matching_p31.get('value')
                            existing_prop.value_text = value_text
                            existing_prop.value_id = value_id
                            existing_prop.property_label = matching_p31.get('property_label', 'instance of')
                            existing_prop.save()
                            fixed_count += 1
                        
                        self.stdout.write(
                            self.style.SUCCESS(f'  ✓ Fixed {fixed_count} broken P31 property(ies)')
                        )
                        p31_found_count += fixed_count
                    else:
                        for p31 in p31_props:
                            value_text, value_id = _normalize_property_value_for_storage(p31.get('value'))
                            Property.objects.create(
                                node=node,
                                property_id='P31',
                                statement_id=p31.get('statement_id'),
                                property_label=p31.get('property_label', 'instance of'),
                                value=p31.get('value'),
                                value_text=value_text,
                                value_id=value_id
                            )
                        
                        self.stdout.write(
                            self.style.SUCCESS(f'  ✓ Added {len(p31_props)} P31 property(ies)')
                        )
                        p31_found_count += len(p31_props)
                    
                    success_count += 1
                else:
                    total_props = len(all_properties)
                    if total_props > 0:
                        self.stdout.write(
                            self.style.WARNING(f'  ⚠ No P31 found (entity exists with {total_props} properties, may be a class/concept)')
                        )
                    else:
                        self.stdout.write(
                            self.style.WARNING(f'  ⚠ No properties returned (entity may not exist in Wikidata)')
                        )
                    success_count += 1
                    p31_not_found_count += 1
                
                if i < total_nodes:
                    time.sleep(delay)
                    
            except Exception as e:
                self.stdout.write(
                    self.style.ERROR(f'  ✗ Error: {str(e)}')
                )
                error_count += 1
                continue
        
        # Summary
        self.stdout.write('')
        self.stdout.write(self.style.SUCCESS('=' * 60))
        self.stdout.write(self.style.SUCCESS('SUMMARY'))
        self.stdout.write(self.style.SUCCESS('=' * 60))
        self.stdout.write(f'Total nodes processed: {total_nodes}')
        self.stdout.write(self.style.SUCCESS(f'Successfully processed: {success_count}'))
        self.stdout.write(self.style.SUCCESS(f'  - Nodes with P31 found: {p31_found_count}'))
        if p31_not_found_count > 0:
            self.stdout.write(self.style.WARNING(f'  - Nodes without P31: {p31_not_found_count}'))
            self.stdout.write(self.style.WARNING('    (These may be classes/concepts without instance types)'))
        if error_count > 0:
            self.stdout.write(self.style.ERROR(f'Errors: {error_count}'))
        self.stdout.write(self.style.SUCCESS('=' * 60))
