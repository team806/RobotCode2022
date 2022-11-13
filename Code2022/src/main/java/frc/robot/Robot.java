// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.SensorUtil;
//import javax.annotation.meta.When;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.BaseMotorController;
//below imports for falcon motors

import org.photonvision.PhotonCamera;

import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;

// below imports are for Limelight CV stuff
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  // definitions
  PhotonCamera camera = new PhotonCamera("806PhotonCamera");

  private final XboxController m_driverController = new XboxController(0);
  private final Joystick m_eagleController = new Joystick(2);
  private static final String northAuto = "North";
  private static final String southAuto = "South";
  private static final String Ball = "oneBall";

  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  static final double MOTOR_TICK_COUNT = 2048;
  double quarterTurn = 2048 / 4;
  double rotationTurn;

  private final WPI_TalonFX m_frontLeft = new WPI_TalonFX(5);
  private final WPI_TalonFX m_rearLeft = new WPI_TalonFX(6);
  MotorControllerGroup m_leftMotor = new MotorControllerGroup(m_frontLeft, m_rearLeft);

  private final WPI_TalonFX m_frontRight = new WPI_TalonFX(3);
  private final WPI_TalonFX m_rearRight = new WPI_TalonFX(2);
  MotorControllerGroup m_rightMotor = new MotorControllerGroup(m_frontRight, m_rearRight);

  private final WPI_TalonFX m_righteagle = new WPI_TalonFX(1);
  private final WPI_TalonFX m_lefteagle = new WPI_TalonFX(7);

  private final WPI_TalonFX m_rightclimber = new WPI_TalonFX(8);
  private final WPI_TalonFX m_leftclimber = new WPI_TalonFX(9);

  private final DifferentialDrive m_myRobot = new DifferentialDrive(m_leftMotor, m_rightMotor);

  private final Timer timer = new Timer();
  private boolean hasBackedUp = false;
  private boolean twoballs = true;

  // boolean buttonToggle = false;
  // motor_Control_Mode = ctre.TalonFXControlMode.Position;

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("North", northAuto);
    m_chooser.addOption("South", southAuto);
    m_chooser.addOption("oneBall", Ball);
    SmartDashboard.putData("Auto choices", m_chooser);

    // We need to invert one side of the drivetrain so that positive voltages
    // result in both sides moving forward. Depending on how your robot's
    // gearbox is constructed, you might have to invert the left side instead.
    m_rightMotor.setInverted(true);

    m_myRobot.close();

    // m_righteagle.follow(m_lefteagle);
    // m_rightclimber.follow(m_leftclimber);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable chooser
   * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
   * remove all of the chooser code and uncomment the getString line to get the
   * auto name from the text box below the Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure below with additional strings. If using the SendableChooser
   * make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    if (DriverStation.getAlliance() == Alliance.Blue) { // we're BLUE team, get RED cargo
      camera.setPipelineIndex(1); // make RED cargo pipeline pipeline index 1
    } else if (DriverStation.getAlliance() == Alliance.Red) { // we're RED team, get BLUE cargo
      camera.setPipelineIndex(2); // make BLUE cargo pipeline pipeline index 2
    }

    m_autoSelected = m_chooser.getSelected();
    m_autoSelected = SmartDashboard.getString("Auto Selector", northAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    switch (m_autoSelected) {
      case southAuto:
        rotationTurn = 0.5;
        break;
      // case Ball:
      // twoballs = false;
      case northAuto:
        rotationTurn = -0.5;
      default:
        break;

    }

    timer.reset();
    timer.start();

  }
  // if (twoballs) {
  // m_myRobot.arcadeDrive(.5, 0);
  // Timer.delay(1);
  // m_myRobot.stopMotor();
  // m_myRobot.arcadeDrive(-.5, 0);
  // Timer.delay(1);
  // m_myRobot.stopMotor();
  // m_myRobot.arcadeDrive(0, rotationTurn);
  // Timer.delay(2);
  // m_myRobot.stopMotor();
  // m_myRobot.arcadeDrive(.5, 0);
  // Timer.delay(3);
  // m_myRobot.stopMotor();
  // } else if (!twoballs) {
  // m_myRobot.arcadeDrive(.5, 0);
  // Timer.delay(5);
  // }

  // if (timer.get() < 1) {
  // m_myRobot.arcadeDrive(.5, 0);

  // }
  // timer.reset();
  // if (timer.get() < 1) {
  // m_myRobot.arcadeDrive(-.5, 0);

  // }
  // timer.reset();
  // if (timer.get() < 2) {
  // m_myRobot.arcadeDrive(0, rotationTurn);

  // }
  // timer.reset();
  // if (timer.get() < 3) {
  // m_myRobot.arcadeDrive(.5, 0);

  // }
  // timer.reset();

  // }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    var result = camera.getLatestResult();
    // COMMENTED BECAUSE WE ARE SMAERT AND TOTALLY BROUGHT THE LIMELIGHT
    if (result.hasTargets()) {
      hasBackedUp = false;

      if ((Math.round(result.getBestTarget().getYaw() * 100.0) / 100.0) > 20) {
        // turn right
        m_myRobot.arcadeDrive(0, 0.1);
      } else if ((Math.round(result.getBestTarget().getYaw() * 100.0) / 100.0) < -20) {
        // turn left
        m_myRobot.arcadeDrive(0, -0.1);
      } else {
        // drive forward
        m_myRobot.arcadeDrive(0.1, 0);
      }
    } else {
      DriverStation.reportWarning("no targets", true);
      // 0 targets. find a target by pivoting enough left/right so that we get
      // target
      // back up robot just a lil bit so we dont collide with an allied color ball
      timer.start();

      if (!hasBackedUp && timer.get() < 1) {
        m_myRobot.arcadeDrive(0.1, 0);
        hasBackedUp = true;
      }

      m_myRobot.arcadeDrive(0, rotationTurn);
      timer.reset();
    }

  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    m_myRobot.arcadeDrive(-m_driverController.getLeftY(), m_driverController.getRightX());

    // if (m_driverController.getLeftBumper() == true) {
    // DriverStation.reportWarning("SHIT", true);
    // }
    // TICK COUNT = 2048

    // if (m_driverController.getLeftBumper() == true
    // && ((BaseMotorController) m_intakeMotor).getSelectedSensorPosition() >=
    // 16384) {
    // m_intakeMotor.set(-0.1);
    // }

    // if (m_driverController.getRightBumper() == true
    // && ((BaseMotorController) m_intakeMotor).getSelectedSensorPosition() <= 0) {
    // m_intakeMotor.set(0.1);
    // }

    // if (m_driverController.getLeftBumperPressed() == true) {
    // buttonToggle = true;
    // }
    // if (m_driverController.getRightBumperPressed() == true) {
    // buttonToggle = false;
    // }

    // if (buttonToggle == true) {
    // if (m_intakeMotor.getSelectedSensorPosition() < 2048) {
    // m_intakeMotor.set(0.1);
    // } else {
    // m_intakeMotor.set(0);
    // }
    // } else if (m_intakeMotor.getSelectedSensorPosition() > 0) {
    // m_intakeMotor.set(-0.1);
    // } else {
    // m_intakeMotor.set(0);
    // }
    // ALL OF THIS SHIT SUCKS

    // Intake is going to be manual for now because encoders make me want to tear my
    // hair out
    // if (m_driverController.getRightBumper() == true) {
    // m_intakeRotator.set(0.4);
    // } else if (m_driverController.getLeftBumper() == true) {
    // m_intakeRotator.set(-0.2);
    // } else {
    // m_intakeRotator.stopMotor();
    // }

    // So are the eagles
    if (m_driverController.getXButton() == true) {
      m_lefteagle.set(-0.3);
      // m_righteagle.follow(m_lefteagle);
    } else if (m_driverController.getYButton() == true) {
      m_lefteagle.set(0.3);
      // m_righteagle.follow(m_lefteagle);
    } else {
      m_lefteagle.stopMotor();
      // m_righteagle.stopMotor();
    }

    if (m_driverController.getXButton() == true) {
      m_righteagle.set(0.2);
    } else if (m_driverController.getYButton() == true) {
      m_righteagle.set(-0.2);
    } else {
      m_righteagle.stopMotor();
    }

    // So are the climbers
    if (m_driverController.getLeftBumper() == true) {
      m_rightclimber.set(0.55);
      // m_rightclimber.follow(m_leftclimber);
    } else if (m_driverController.getRightBumper() == true) {
      m_rightclimber.set(-0.55);
      // m_rightclimber.follow(m_leftclimber);
    } else {
      m_rightclimber.stopMotor();
      // m_rightclimber.stopMotor();
    }

    // To make the intake actually take in
    // if (m_driverController.getStartButton() == true) {
    // m_intakeMotor.set(1);
    // } else if (m_driverController.getBackButton() == true) {
    // m_intakeMotor.set(-0.7);
    // } else {
    // m_intakeMotor.stopMotor();
    // }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
  }

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
  }
}