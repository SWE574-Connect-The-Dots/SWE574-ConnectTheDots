from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0022_property_value_fields'),
    ]

    operations = [
        migrations.CreateModel(
            name='EdgeProperty',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('property_id', models.CharField(max_length=255)),
                ('statement_id', models.CharField(default=None, max_length=255, null=True)),
                ('property_label', models.CharField(blank=True, max_length=255, null=True)),
                ('value', models.JSONField(blank=True, null=True)),
                ('value_text', models.TextField(blank=True, null=True)),
                ('value_id', models.CharField(blank=True, max_length=255, null=True)),
                ('edge', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='edge_properties', to='api.edge')),
            ],
            options={
                'unique_together': {('edge', 'statement_id')},
            },
        ),
    ]
