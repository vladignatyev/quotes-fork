# Generated by Django 2.2.5 on 2019-11-22 12:16

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0017_auto_20191122_1155'),
    ]

    operations = [
        migrations.AlterModelOptions(
            name='balancerechargeproduct',
            options={'verbose_name': 'Продукт «Монеты»', 'verbose_name_plural': 'продукты «монеты»'},
        ),
        migrations.AlterModelOptions(
            name='tag',
            options={'verbose_name': 'тег', 'verbose_name_plural': 'теги'},
        ),
    ]
