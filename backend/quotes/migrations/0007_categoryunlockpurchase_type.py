# Generated by Django 2.2.5 on 2019-10-10 18:08

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0006_auto_20191009_2238'),
    ]

    operations = [
        migrations.AddField(
            model_name='categoryunlockpurchase',
            name='type',
            field=models.CharField(choices=[('null_unlock', '!! не использовать !!'), ('unlock_for_coins', 'Анлок за игровые монеты'), ('unlock_by_purchase', 'Анлок за покупку')], default='null_unlock', max_length=32),
        ),
    ]
