# Generated by Django 2.2.5 on 2020-02-13 11:33

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0018_auto_20200212_1005'),
    ]

    operations = [
        migrations.AlterField(
            model_name='pushnotificationqueueitem',
            name='push_subscription',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.CASCADE, to='api.PushSubscription'),
        ),
    ]
