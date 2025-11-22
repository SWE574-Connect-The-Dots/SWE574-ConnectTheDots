from django.db import migrations, models
import django.utils.timezone


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0019_edge_created_at_node_created_at'),
    ]

    operations = [
        migrations.CreateModel(
            name='Activity',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('as2_id', models.CharField(blank=True, max_length=255, null=True)),
                ('type', models.CharField(max_length=64)),
                ('actor', models.CharField(max_length=255)),
                ('object', models.CharField(max_length=255)),
                ('target', models.CharField(blank=True, max_length=255, null=True)),
                ('summary', models.TextField(blank=True, default='')),
                ('published', models.DateTimeField(default=django.utils.timezone.now)),
                ('to', models.JSONField(blank=True, default=list)),
                ('cc', models.JSONField(blank=True, default=list)),
                ('payload', models.JSONField(blank=True, default=dict)),
            ],
        ),
        migrations.AddIndex(
            model_name='activity',
            index=models.Index(fields=['published'], name='api_activit_publish_7d0ac6_idx'),
        ),
        migrations.AddIndex(
            model_name='activity',
            index=models.Index(fields=['type'], name='api_activit_type_f049ed_idx'),
        ),
        migrations.AddIndex(
            model_name='activity',
            index=models.Index(fields=['actor'], name='api_activit_actor_97e164_idx'),
        ),
        migrations.AddIndex(
            model_name='activity',
            index=models.Index(fields=['object'], name='api_activit_object_3e45a7_idx'),
        ),
    ]

