package calypso;

import robocode.*;
import java.awt.*;

public class Joelma extends AdvancedRobot {
	int moveDirection=1;

	public void run() {
		setAdjustRadarForRobotTurn(true); //Essa função deixa o radar travado enquanto o robô gira
		setBodyColor(new Color(255,0,255));
		setGunColor(Color.yellow);
		setRadarColor(Color.yellow);
		setBulletColor(Color.pink);
		setAdjustGunForRobotTurn(true); //Essa função deixa o canhão travado enquanto o robô gira
		turnRadarRightRadians(Double.POSITIVE_INFINITY); //Gira o radar pra direita
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing=e.getBearingRadians()+getHeadingRadians(); //Angulação do ponto médio de todos os inimigos da tela
		double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -absBearing);//ultima velocidade dos inimigos aferida 
		double gunTurnAmt;//valor de giro do canhão
		double previousEnergy = 100;
		int movementDirection = 1; //utilizado para o calculo de desvio de bala 
		int gunDirection = 1; // para onde girar o canhão 
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//travando o radar 
		if(Math.random()>.9){
			setMaxVelocity((12*Math.random())+12);//Mudando aletoriamente a velocidade do robô para tentar fugir das balas
		}
		//Se pegar um inimigo como um scanned robot event
		if (e.getDistance() > 150) {//se a distancia for maior que 150, ajusta o canhão so um pouco e vai pro inimigo
			gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/22);
			setTurnGunRightRadians(gunTurnAmt); 
			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity())); //tenta prever a possivel localização futura
			setAhead((e.getDistance() - 140)*moveDirection);
			setFire(3);
		}
		else{//Senão: já esta perto, gira a arma pois vamos tentar ficar a 90 graus do inimigo para tentar desviar das balas
			gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/15);
			setTurnGunRightRadians(gunTurnAmt);
			setTurnLeft(-90-e.getBearing()); 
			setAhead((e.getDistance() - 140)*moveDirection);
			setFire(3);
		}	
		//Vamos tentar detectar um tiro com base na mudança de energia do robo inimigo
		double changeInEnergy = previousEnergy-e.getEnergy();
		if (changeInEnergy>0 && changeInEnergy<=3) { //se mudou a energia e não mudou tanto a ponto de ter levado um tiro, inverto minha posição
			movementDirection = -movementDirection;
			setAhead((e.getDistance()/4+25)*movementDirection);
			DEBUG.mensaje("dança joelminha, dança");//
		}
	}
	public void onHitWall(HitWallEvent e){ //quando recebe o evento que bateu numa parede, inverte a posição que tava indo
		moveDirection=-moveDirection;
	}

	public void onWin(WinEvent e) { //na vitória ela dança e canta :D
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
			DEBUG.mensaje("Isso e muito mais você só vai encontrar no pará!");
		}
	}

}