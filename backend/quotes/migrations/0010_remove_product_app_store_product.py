# Generated by Django 2.2.5 on 2019-10-02 11:45

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0009_auto_20191002_1144'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='product',
            name='app_store_product',
        ),
    ]
