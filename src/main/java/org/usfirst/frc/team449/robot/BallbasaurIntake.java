package org.usfirst.frc.team449.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;
import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.usfirst.frc.team449.robot.jacksonWrappers.YamlSubsystem;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.usfirst.frc.team449.robot.jacksonWrappers.MappedDoubleSolenoid;
import org.usfirst.frc.team449.robot.subsystem.interfaces.intake.SubsystemIntake;
import org.usfirst.frc.team449.robot.subsystem.interfaces.solenoid.SubsystemSolenoid;

public class BallbasaurIntake extends YamlSubsystem implements SubsystemSolenoid, SubsystemIntake {

	/**
	 * Piston for pushing gears
	 */
	@NotNull
	private final DoubleSolenoid piston;

	/**
	 * The piston's current position
	 */
	private DoubleSolenoid.Value pistonPos;
	/**
	 * first:
	 */
	private final double intakeRollerSpeedSlow;
	private final double intakeRollerSpeedFast;
	/**
	 * Default constructor
	 *
	 * @param piston The piston that comprises this subsystem.
	 */
	@JsonCreator
	public BallbasaurIntake(@NotNull @JsonProperty(required = true) MappedDoubleSolenoid piston,
	                        double intakeRollerSpeedSlow, double intakeRollerSpeedFast) {
		this.piston = piston;
		this.intakeRollerSpeedFast = intakeRollerSpeedFast;
		this.intakeRollerSpeedSlow = intakeRollerSpeedSlow;
	}


	/**
	 * @param value The position to set the solenoid to.
	 */
	public void setSolenoid(@NotNull DoubleSolenoid.Value value){
			piston.set(value);
			pistonPos= value;
	}

	/**
	 * @return the current position of the solenoid.
	 */
	@NotNull
	public DoubleSolenoid.Value getSolenoidPosition(){
		return pistonPos;
	}

	@Override
	protected void initDefaultCommand() {
	}

	@NotNull
	@Override
	public IntakeMode getMode() {
		return null;
	}

	@Override
	public void setMode(@NotNull IntakeMode mode) {

	}
}
