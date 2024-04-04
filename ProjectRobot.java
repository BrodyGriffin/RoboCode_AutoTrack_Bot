package BJ;
import robocode.*;
import java.awt.Color;
import java.lang.Math;
import robocode.util.Utils;

 // ProjectRobot - a robot by (Brody Griffin)
 /*
 	 robot movement inspired by walls sample robot 
 */
public class ProjectRobot extends AdvancedRobot {
	//control of robot behavior
    private boolean scanningEnabled;
    private boolean avoidingCollision;
	private double fieldSize;
	private RobotStatus robotStatus;
	

    public void run() {
	//setup robot to begin battle
        initializeRobot();
		//infinite loop with battle instructions
        while (true) {
			//move according to rules
			
			moveAlongWall();
			turnRight(90);

			// Set a threshold distance from the wall before making a gradual turn
        /*	double thresholdDistance = 500; // Adjust this threshold as needed

            double yDifference = getBattleFieldHeight() - getY();
			//vertical wall
			moveAlongWall();
			if ((yDifference <= thresholdDistance) || (getY() <= thresholdDistance)) { 
				setTurnRight(90);
				execute();
			}
		  
			double  xDifference = getBattleFieldWidth() - getX();
			//horizontal wall
       	    moveAlongWall();	
			if ((xDifference <= thresholdDistance) || (getX() <= thresholdDistance)) { 
				setTurnRight(90);
				execute();*/
			}
        }
    }

	//normal movement
	private void moveAlongWall() {
		// if no robots in the way move ahead the size of the field
		 if (!avoidingCollision) {
		    	ahead(fieldSize);
		 }
	}

	// enemy robot in path of travel
    public void onHitRobot(HitRobotEvent e) {
		//currently avoiding a collision
		//movement should not carry on as usual
		//move away from enemy
        avoidingCollision = true;
		//check where the enemy is to move the correct direction
        if (Math.abs(e.getBearing()) > 90) {
			//move ahead
            ahead(100);
			adjustInitialPosition();
        } 
		else {
			//move backwards
            back(100);
			adjustInitialPosition();
        }
		//collision avoided
		//normal movement can resume
        avoidingCollision = false;
    }

	//enemy robot found
    public void onScannedRobot(ScannedRobotEvent e) {
		double angleToEnemy = e.getBearing();
		// angle to scanned robot
		double angle = Math.toRadians(robotStatus.getHeading() + angleToEnemy % 360);
		//CO-Ordinates
		double xEnemy = (robotStatus.getX() + Math.cos(angle) * e.getDistance());
		double yEnemy = (robotStatus.getY() + Math.sin(angle) * e.getDistance());

		double degrees = calcShot(e.getVelocity(), xEnemy, yEnemy,e.getHeading());
		
		turnGunRight(degrees - getGunHeading());
		// fire with 2 power
        fire(2);
		//if not moving into position scan manually
        if (scanningEnabled) {
            scan();
        }
    }

	//robot setup
    private void initializeRobot() {
		//size of playing field
		fieldSize = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		//robot color scheme (all white)
        setColors(Color.white, Color.white, Color.white, Color.white, Color.white);
		//scanning allowed 
        scanningEnabled = true;
		//initially not avoiding a collision
        avoidingCollision = false;
        adjustInitialPosition();
    }


	//position setup 
    private void adjustInitialPosition() {
		//temporarily disable scan
        scanningEnabled = false;
		//find a wall
        turnLeft(getHeading() % 180);
		//go to the wall
        ahead(fieldSize);
		//face tank and gun toward the centre
        turnGunRight(90);
        turnRight(90);
		//get to corner 
		ahead(fieldSize);
		//re-enable scanning
        scanningEnabled = true;
    }


	public void onStatus(StatusEvent e) {	
		this.robotStatus = e.getStatus();
		if (scanningEnabled) {
			setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		}
	}


	private double calcShot(double vEnemy, double xEnemy, double yEnemy, double EnemyHeading) {
		double vBullet = 14;
		double time_delay = 0.008;
		double Theta_enemy = EnemyHeading * (Math.PI/180);
		double vEnemy_y = vEnemy*Math.sin(Theta_enemy);
		double vEnemy_x = vEnemy*Math.cos(Theta_enemy);
		

		double x_max = getBattleFieldWidth();
		double y_max = getBattleFieldHeight();
		
		double xMe = getX();
		double yMe = getY();
		double vMe = getVelocity();
		
		double VxBot = 0;
    	double VyBot = 0;
		if (xMe == x_max) {
    		VxBot = 0;
    		VyBot = vMe;
		}
		else if (yMe == y_max) {
    		VxBot = -vMe;
    		VyBot = 0;
		}
		else if (xMe == 0) {
    		VxBot = 0;
		    VyBot = -vMe;
		}
		else if (yMe == 0) {
		    VxBot = vMe;
		    VyBot = 0;
		}

		// Where does the bullet start from
		double yb = VyBot * time_delay + yMe;
		double xb = VxBot * time_delay + xMe;
		
		// Quadratic equation
		double a = Math.pow(vEnemy,2) - Math.pow(vBullet,2);
		double b = (2 * vEnemy_y * (yEnemy + (vEnemy_y * time_delay) - yb)) + ( 2 * vEnemy_x * (xEnemy + (vEnemy_x * time_delay) - xb));
		double c = (yEnemy + Math.pow(((vEnemy_y * time_delay) - yb),2)) + (Math.pow((xEnemy + (vEnemy_x * time_delay) - xb),2));
		double t1 = (-b - Math.sqrt(Math.pow(b,2) - 4 * a * c)) / (2 * a);
		double t2 = (-b + Math.sqrt(Math.pow(b,2) - 4 * a * c))/(2 * a);
		double t;
		if ((t2 > t1) && (t1 > 0)) {
		    t = t1;
		}
		else if (t2 > 0) {
		    t = t2;
		}
		else if (t1 > 0) {
		    t = t1;
		}
		else{
		    t = 0;
		}
		    
		double gamma = 0;
		if (t > 0) {
		    double y3 = vEnemy_y * (t + time_delay) + yEnemy;
		    double x3 = vEnemy_x * (t + time_delay) + xEnemy;
			
			double x = x3 - xb;
			double y = y3 - yb;
			
			gamma = Math.atan2(x,y) + (3 * Math.PI/2);
  			while (gamma >= 2 * Math.PI) {
				gamma -= 2 * Math.PI;
			}
			gamma = 2 * Math.PI - gamma;
			
			double vBullet_y = vBullet * Math.sin(gamma);
		    double vBullet_x = vBullet * Math.cos(gamma);
		    double y4 = vBullet_y * t + yb;
		    double x4 = vBullet_x * t + xb;
		}
		return gamma * (180/Math.PI);
	}
}
