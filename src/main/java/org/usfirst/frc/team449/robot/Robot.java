package org.usfirst.frc.team449.robot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.command.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.usfirst.frc.team449.robot.drive.unidirectional.DriveTalonCluster;
import org.usfirst.frc.team449.robot.other.Clock;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * The main class of the robot, constructs all the subsystems and initializes default commands.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.StringIdGenerator.class)
public class Robot extends IterativeRobot {

	/**
	 * The absolute filepath to the resources folder containing the config files.
	 */
	@NotNull
	public static final String RESOURCES_PATH = "/home/lvuser/449_resources/";

	/**
	 * The drive
	 */
	private DriveTalonCluster driveSubsystem;

	/**
	 * The object constructed directly from the yaml map.
	 */
	private RobotMap robotMap;

	/**
	 * The Notifier running the logging thread.
	 */
	private Notifier loggerNotifier;

	/**
	 * The string version of the alliance we're on ("red" or "blue"). Used for string concatenation to pick which
	 * profile to execute.
	 */
	@Nullable
	private String allianceString;

	/**
	 * The I2C channel for communicating with the RIOduino.
	 */
	@Nullable
	private I2C robotInfo;

	/**
	 * Whether or not the robot has been enabled yet.
	 */
	private boolean enabled;

	/**
	 * The method that runs when the robot is turned on. Initializes all subsystems from the map.
	 */
	public void robotInit() {
		//Set up start time
		Clock.setStartTime();
		Clock.updateTime();

		enabled = false;

		//Yes this should be a print statement, it's useful to know that robotInit started.
		System.out.println("Started robotInit.");

		Yaml yaml = new Yaml();
		try {
			//Read the yaml file with SnakeYaml so we can use anchors and merge syntax.
//			Map<?, ?> normalized = (Map<?, ?>) yaml.load(new FileReader(RESOURCES_PATH+"ballbasaur_map.yml"));
//			Map<?, ?> normalized = (Map<?, ?>) yaml.load(new FileReader(RESOURCES_PATH + "naveen_map.yml"));
//			Map<?, ?> normalized = (Map<?, ?>) yaml.load(new FileReader(RESOURCES_PATH + "nate_map.yml"));
			Map<?, ?> normalized = (Map<?, ?>) yaml.load(new FileReader(RESOURCES_PATH + "calcifer_outreach_map.yml"));
			YAMLMapper mapper = new YAMLMapper();
			//Turn the Map read by SnakeYaml into a String so Jackson can read it.
			String fixed = mapper.writeValueAsString(normalized);
			//Use a parameter name module so we don't have to specify name for every field.
			mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
			//Deserialize the map into an object.
			robotMap = mapper.readValue(fixed, RobotMap.class);
		} catch (IOException e) {
			//This is either the map file not being in the file system OR it being improperly formatted.
			System.out.println("Config file is bad/nonexistent!");
			e.printStackTrace();
		}

		//Read sensors
		this.robotMap.getUpdater().run();

		//Set fields from the map.
		this.loggerNotifier = new Notifier(robotMap.getLogger());
		this.driveSubsystem = robotMap.getDrive();

		//Set up RIOduino I2C channel if it's in the map.
		if (robotMap.getRIOduinoPort() != null) {
			robotInfo = new I2C(I2C.Port.kOnboard, robotMap.getRIOduinoPort());
		}

		//Set up the motion profiles if we're doing motion profiling

		//Run the logger to write all the events that happened during initialization to a file.
		robotMap.getLogger().run();
		Clock.updateTime();
	}

	/**
	 * Run when we first enable in teleop.
	 */
	@Override
	public void teleopInit() {
		//Do the startup tasks
		doStartupTasks();

		//Read sensors
		this.robotMap.getUpdater().run();

		//Run startup command if we start in teleop.
		if (!enabled) {
			if (robotMap.getStartupCommand() != null) {
				robotMap.getStartupCommand().start();
			}
			enabled = true;
		}

		driveSubsystem.stopMPProcesses();

		if (robotMap.getTeleopStartupCommand() != null) {
			robotMap.getTeleopStartupCommand().start();
		}

		//Set the default command
		driveSubsystem.setDefaultCommandManual(robotMap.getDefaultDriveCommand());
	}

	/**
	 * Run every tick in teleop.
	 */
	@Override
	public void teleopPeriodic() {
		//Refresh the current time.
		Clock.updateTime();

		//Read sensors
		this.robotMap.getUpdater().run();

		//Run all commands. This is a WPILib thing you don't really have to worry about.
		Scheduler.getInstance().run();
	}

	/**
	 * Run when we first enable in autonomous
	 */
	@Override
	public void autonomousInit() {
		//Do startup tasks
		doStartupTasks();

		//Read sensors
		this.robotMap.getUpdater().run();

		//Run startup command if we start in auto.
		if (!enabled) {
			if (robotMap.getStartupCommand() != null) {
				robotMap.getStartupCommand().start();
			}
			enabled = true;
		}

		//Tell the RIOduino we're in autonomous
		sendModeOverI2C(robotInfo, "auto");
	}

	/**
	 * Runs every tick in autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		//Update the current time
		Clock.updateTime();
		//Read sensors
		this.robotMap.getUpdater().run();
		//Run all commands. This is a WPILib thing you don't really have to worry about.
		Scheduler.getInstance().run();
	}

	/**
	 * Run when we disable.
	 */
	@Override
	public void disabledInit() {
		//Fully stop the drive
		driveSubsystem.fullStop();
		//Tell the RIOduino we're disabled.
		sendModeOverI2C(robotInfo, "disabled");
	}

	/**
	 * Run when we first enable in test mode.
	 */
	@Override
	public void testInit() {
		//Run startup command if we start in test mode.
		if (!enabled) {
			if (robotMap.getStartupCommand() != null) {
				robotMap.getStartupCommand().start();
			}
			enabled = true;
		}
	}

	/**
	 * Run every tic while disabled
	 */
	@Override
	public void disabledPeriodic() {
		Clock.updateTime();
		//Read sensors
		this.robotMap.getUpdater().run();
	}

	/**
	 * Sends the current mode (auto, teleop, or disabled) over I2C.
	 *
	 * @param i2C  The I2C channel to send the data over.
	 * @param mode The current mode, represented as a String.
	 */
	private void sendModeOverI2C(I2C i2C, String mode) {
		//If the I2C exists
		if (i2C != null) {
			//Turn the alliance and mode into a character array.
			char[] CharArray = (allianceString + "_" + mode).toCharArray();
			//Transfer the character array to a byte array.
			byte[] WriteData = new byte[CharArray.length];
			for (int i = 0; i < CharArray.length; i++) {
				WriteData[i] = (byte) CharArray[i];
			}
			//Send the byte array.
			i2C.transaction(WriteData, WriteData.length, null, 0);
		}
	}

	/**
	 * Do tasks that should be done when we first enable, in both auto and teleop.
	 */
	private void doStartupTasks() {
		//Refresh the current time.
		Clock.updateTime();

		//Start running the logger
		loggerNotifier.startPeriodic(robotMap.getLogger().getLoopTimeSecs());
	}
}
