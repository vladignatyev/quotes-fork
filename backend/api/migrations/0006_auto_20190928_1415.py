# Generated by Django 2.2.5 on 2019-09-28 14:15

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0005_auto_20190928_1358'),
    ]

    operations = [
        migrations.RenameField(
            model_name='appstoreiappurchase',
            old_name='appstore_product',
            new_name='product',
        ),
        migrations.RenameField(
            model_name='googleplayiappurchase',
            old_name='google_play_product',
            new_name='product',
        ),
    ]
