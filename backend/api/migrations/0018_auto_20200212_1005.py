# Generated by Django 2.2.5 on 2020-02-12 10:05

import django.contrib.postgres.fields.jsonb
from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0017_auto_20200212_1004'),
    ]

    operations = [
        migrations.AlterField(
            model_name='pushmessage',
            name='data',
            field=django.contrib.postgres.fields.jsonb.JSONField(blank=True),
        ),
    ]
