package com.example.home_pc.myjobscheduler

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioGroup
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import com.example.home_pc.myjobscheduler.notificationscheduler.NotificationJobService
import android.content.ComponentName
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Switch
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val JOB_ID = 0
    }

    lateinit var mScheduler: JobScheduler
    lateinit var mDeviceIdleSwitch: Switch
    lateinit var mDeviceChargingSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSchedule.setOnClickListener { scheduleJob() }
        btnCancelSchedule.setOnClickListener { cancelJobs() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i > 0){
                    seekBarProgress.setText(i.toString() + "s");
                }else {
                    seekBarProgress.setText("Not Set");
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

    }


    fun scheduleJob() {
        var selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE

        val networkOptions = findViewById<RadioGroup>(R.id.networkOptions)
        val selectedNetworkID = networkOptions.checkedRadioButtonId

        when (selectedNetworkID) {
            R.id.noNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE
            R.id.anyNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY
            R.id.wifiNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED
        }

        mScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler;


        val serviceName = ComponentName(packageName, NotificationJobService::class.java.name)
        val builder = JobInfo.Builder(JOB_ID, serviceName)
        builder.setRequiredNetworkType(selectedNetworkOption)

        builder.setRequiresDeviceIdle(idleSwitch.isChecked)
        builder.setRequiresCharging(chargingSwitch.isChecked)

        val seekBarInteger:Long = seekBar.progress.toLong()
        val seekBarSet = seekBarInteger > 0

        if (seekBarSet) {
            builder.setOverrideDeadline(seekBarInteger * 1000);
        }

        val constraintSet = (selectedNetworkOption !== JobInfo.NETWORK_TYPE_NONE
                || mDeviceChargingSwitch.isChecked || mDeviceIdleSwitch.isChecked) || seekBarSet

        when(constraintSet){
            true -> {
                val myJobInfo = builder.build()
                mScheduler.schedule(myJobInfo)
            }
            else -> Toast.makeText(this, "Job Scheduled, job will run when " +
                    "the constraints are met.", Toast.LENGTH_SHORT).show();
        }

    }

    fun cancelJobs(){
            mScheduler.cancelAll();
            Toast.makeText(this, "Jobs cancelled", Toast.LENGTH_SHORT).show();
    }
}
