# Generated by Django 2.2.5 on 2019-11-20 19:35

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0011_auto_20191120_1933'),
    ]

    operations = [
        migrations.AlterField(
            model_name='topic',
            name='is_published',
            field=models.BooleanField(blank=True, default=True, verbose_name='Тема опубликована?'),
        ),
    ]
