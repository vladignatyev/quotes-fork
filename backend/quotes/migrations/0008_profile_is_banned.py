# Generated by Django 2.2.5 on 2019-11-20 16:11

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0007_categoryunlockpurchase_status'),
    ]

    operations = [
        migrations.AddField(
            model_name='profile',
            name='is_banned',
            field=models.BooleanField(blank=True, default=False, verbose_name='Забанен?'),
        ),
    ]
