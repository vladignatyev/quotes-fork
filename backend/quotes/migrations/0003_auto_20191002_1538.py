# Generated by Django 2.2.5 on 2019-10-02 15:38

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0002_categoryunlockpurchase'),
    ]

    operations = [
        migrations.RenameField(
            model_name='categoryunlockpurchase',
            old_name='google_play_product',
            new_name='google_play_purchase',
        ),
    ]
