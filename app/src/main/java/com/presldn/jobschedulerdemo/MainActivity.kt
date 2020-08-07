package com.presldn.jobschedulerdemo

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private val JOB_ID = 0
    private var scheduler: JobScheduler? = null

    private lateinit var deviceIdleSwitch: Switch
    private lateinit var deviceChargingSwitch: Switch

    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceIdleSwitch = findViewById(R.id.idleSwitch)
        deviceChargingSwitch = findViewById(R.id.chargingSwitch)
        seekBar = findViewById(R.id.seekBar)

        val seekBarProgress = findViewById<TextView>(R.id.seekBarProgress)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress > 0) {
                    seekBarProgress.text = getString(R.string.seek_bar_progress, progress)
                } else {
                    seekBarProgress.text = getString(R.string.not_set)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

    }

    fun scheduleJob(view: View) {
        val networkOptions: RadioGroup = findViewById(R.id.networkOptions)

        val selectedNetworkId = networkOptions.checkedRadioButtonId

        var selectedNetworkOption: Int = JobInfo.NETWORK_TYPE_NONE

        val seekBarInteger = seekBar.progress

        val seekBarSet = seekBarInteger > 0

        when (selectedNetworkId) {
            R.id.noNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE
            R.id.anyNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY
            R.id.wifiNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED
        }

        scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val serviceName = ComponentName(
            packageName,
            NotificationJobService::class.java.name
        )

        val builder = JobInfo.Builder(JOB_ID, serviceName)

        if (seekBarSet) {
            builder.setOverrideDeadline((seekBarInteger * 1000).toLong())
        }

        builder.setRequiredNetworkType(selectedNetworkOption)

        val constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE) ||
                    deviceChargingSwitch.isChecked || deviceIdleSwitch.isChecked || seekBarSet

        if (constraintSet) {

            val jobInfo = builder
                .setRequiresDeviceIdle(deviceIdleSwitch.isChecked)
                .setRequiresCharging(deviceChargingSwitch.isChecked)
                .build()

            scheduler?.schedule(jobInfo)

            Toast.makeText(this, getString(R.string.job_schedule_toast_message),
                Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(this, getString(R.string.set_constraint), Toast.LENGTH_SHORT).show()
        }


    }

    fun cancelJobs(view: View) {

        scheduler?.cancelAll();
        scheduler = null
        Toast.makeText(this, getString(R.string.job_cancelled_toast_message), Toast.LENGTH_SHORT)
            .show();

    }
}