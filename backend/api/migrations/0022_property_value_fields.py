from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0021_archive_and_more'),
    ]

    operations = [
        migrations.AddField(
            model_name='property',
            name='property_label',
            field=models.CharField(blank=True, max_length=255, null=True),
        ),
        migrations.AddField(
            model_name='property',
            name='value',
            field=models.JSONField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='property',
            name='value_id',
            field=models.CharField(blank=True, max_length=255, null=True),
        ),
        migrations.AddField(
            model_name='property',
            name='value_text',
            field=models.TextField(blank=True, null=True),
        ),
    ]
