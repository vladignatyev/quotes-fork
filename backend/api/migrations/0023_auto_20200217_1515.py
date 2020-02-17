# Generated by Django 2.2.5 on 2020-02-17 15:15

import django.contrib.postgres.fields.jsonb
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0022_auto_20200217_1513'),
    ]

    operations = [
        migrations.AlterField(
            model_name='pushmessage',
            name='data',
            field=django.contrib.postgres.fields.jsonb.JSONField(blank=True, null=True),
        ),
        migrations.AlterField(
            model_name='pushmessage',
            name='friday',
            field=models.TimeField(blank=True, null=True, verbose_name='Friday'),
        ),
        migrations.AlterField(
            model_name='pushmessage',
            name='saturday',
            field=models.TimeField(blank=True, null=True, verbose_name='Saturday'),
        ),
        migrations.AlterField(
            model_name='pushmessage',
            name='sunday',
            field=models.TimeField(blank=True, null=True, verbose_name='Sunday'),
        ),
        migrations.AlterField(
            model_name='pushmessage',
            name='thursday',
            field=models.TimeField(blank=True, null=True, verbose_name='Thursday'),
        ),
        migrations.AlterField(
            model_name='pushmessage',
            name='tuesday',
            field=models.TimeField(blank=True, null=True, verbose_name='Tuesday'),
        ),
        migrations.AlterField(
            model_name='pushmessage',
            name='wednesday',
            field=models.TimeField(blank=True, null=True, verbose_name='Wednesday'),
        ),
    ]
