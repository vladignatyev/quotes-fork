# Generated by Django 2.2.5 on 2019-11-29 11:49

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0024_auto_20191127_2015'),
    ]

    operations = [
        migrations.AddField(
            model_name='quotecompletion',
            name='hints_used',
            field=models.PositiveIntegerField(default=0),
        ),
        migrations.AlterField(
            model_name='quotecompletion',
            name='kind',
            field=models.CharField(choices=[('normal', 'Normally completed level.'), ('skipped', 'Skipped the level.'), ('not_yet', 'Not yet complete.')], default='not_yet', max_length=16),
        ),
    ]
