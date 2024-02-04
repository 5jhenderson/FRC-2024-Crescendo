// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


import com.ctre.phoenix6.Utils;
import com.ctre.phoenix6.mechanisms.swerve.SwerveModule;
import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.*;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;

//import frc.robot.Commands.TurnInDirectionOfTarget;
import frc.robot.generated.TunerConstants;

import java.util.HashMap;


/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer
{
    private double MaxSpeed = 0.5; // 6 meters per second desired top speed
    private double MaxAngularRate = 1.5 * Math.PI; // 3/4 of a rotation per second max angular velocity

    /* Setting up bindings for necessary control of the swerve drive platform */
    private final CommandXboxController joystick = new CommandXboxController(0); // My joystick
    private final CommandSwerveDrivetrain drivetrain = TunerConstants.DriveTrain; // My drivetrain

    private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
            .withDeadband(MaxSpeed * 0.4).withRotationalDeadband(MaxAngularRate * 0.5) // Add a 10% deadband
            .withDriveRequestType(SwerveModule.DriveRequestType.OpenLoopVoltage); // I want field-centric
    // driving in open loop
    private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
    private final SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();
    private final Telemetry logger = new Telemetry(MaxSpeed);

    // -- Subsystems 


    // -- Auto
    // private SwerveAutoBuilder AutoBuilder = null;
    private final SendableChooser<Command> AutoChooser = new SendableChooser<>();


    public RobotContainer()
    {
        ConfigureBindings();
        SetDefaultCommands();
        // ConfigureSwerveAutoBuilder();
        // ConfigureAutoCommands();
    }


    public Command GetAutonomousCommand()
    {
        return AutoChooser.getSelected();
    }


    private void ConfigureBindings()
    {
        // ----------------------------------------------------------------------------------------
        // -- Driver
        // ----------------------------------------------------------------------------------------


        // -- Auto Turn
//        new Trigger(OI.DriverLeft::getTrigger).whileTrue(new TurnInDirectionOfTarget(_Drive));

        // Zero IMU
//       new Trigger(OI.DriverLeft::getTop).onTrue(new InstantCommand(IMU::zeroIMU));



        // ----------------------------------------------------------------------------------------
        // -- Operator
        // ----------------------------------------------------------------------------------------
        
        // -- Vision
        //new Trigger(OI.Operator::getBackButton).onTrue(LimelightVision.SetPipelineCommand(0).ignoringDisable(true));
        new Trigger(OI.Operator::getStartButton).onTrue(LimelightVision.SetPipelineCommand(1).ignoringDisable(true));
        new Trigger(OI.Operator::getRightStickButton).onTrue(LimelightVision.SetPipelineCommand(2).ignoringDisable(true));

    }


    private void SetDefaultCommands()
    {
        drivetrain.setDefaultCommand( // Drivetrain will execute this command periodically
                drivetrain.applyRequest(() -> drive.withVelocityX(-joystick.getLeftY() * MaxSpeed) // Drive forward with
                        // negative Y (forward)
                        .withVelocityY(-joystick.getLeftX() * MaxSpeed) // Drive left with negative X (left)
                        .withRotationalRate(-joystick.getRightX() * MaxAngularRate) // Drive counterclockwise with negative X (left)
                ));

        joystick.a().whileTrue(drivetrain.applyRequest(() -> brake));
        joystick.b().whileTrue(drivetrain
                .applyRequest(() -> point.withModuleDirection(new Rotation2d(-joystick.getLeftY(), -joystick.getLeftX()))));

        // reset the field-centric heading on left bumper press
        joystick.leftBumper().onTrue(drivetrain.runOnce(() -> drivetrain.seedFieldRelative()));

        if (Utils.isSimulation()) {
            drivetrain.seedFieldRelative(new Pose2d(new Translation2d(), Rotation2d.fromDegrees(90)));
        }
        drivetrain.registerTelemetry(logger::telemeterize);
    }


    // private void ConfigureSwerveAutoBuilder()
    // {
    //     // -- Map Path Planner events to Commands
    //     HashMap<String, Command> eventMap = new HashMap<>();

    //     // eventMap.put("BalanceForward", _Drive.Command_AutoBalance(DriveSubsystem.Direction.Forward));
    //     // eventMap.put("BalanceBackward", _Drive.Command_AutoBalance(DriveSubsystem.Direction.Reverse));

    //     eventMap.put("Wait", new WaitCommand(1));

    //     // -- Builder
    //     AutoBuilder = new SwerveAutoBuilder(
    //             _Drive::GetPose, // Pose2d supplier
    //             _Drive::HardResetOdometry, // Pose2d consumer, used to reset odometry at the beginning of auto
    //             _Drive.GetKinematics(), // SwerveDriveKinematics
    //             Constants.Drive.HOLONOMIC_CONTROLLER_PID_XY_CONSTRAINTS, // PID constants to correct for translation error (used to create the X and Y PID controllers)
    //             Constants.Drive.HOLONOMIC_CONTROLLER_PID_ROTATIONAL_CONSTRAINTS, // PID constants to correct for rotation error (used to create the rotation controller)
    //             _Drive::setModuleStates, // Module states consumer used to output to the drive subsystem
    //             eventMap,
    //             true, // Should the path be automatically mirrored depending on alliance color. Optional, defaults to true
    //         _Drive // The drive subsystem. Used to properly set the requirements of path following commands
    //     );
    // }

    // private Command GetPathPlannerAutoCommand(String name)
    // {
    //     return AutoBuilder.fullAuto(PathPlanner.loadPathGroup(name, new PathConstraints(1, .5)));
    // }

    // private void ConfigureAutoCommands()
    // {
    //     AutoChooser.setDefaultOption("No auto", new WaitUntilCommand(0));
    //     SmartDashboard.putData(AutoChooser);
    // }

}


