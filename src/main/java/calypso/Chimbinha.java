package calypso;

import java.awt.Color;
import java.util.Vector;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResultsRow;
import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class Chimbinha extends AdvancedRobot {

    public static String FICHERO_REGLAS = "calypso/reglas/reglas_robot.drl";
    public static String CONSULTA_ACCIONES = "consulta_acciones";
    
    private KnowledgeBuilder kbuilder;
    private KnowledgeBase kbase;   // Base de conhecimentos
    private StatefulKnowledgeSession ksession;  // seriam os conhecimentos da sessão atual
    private Vector<FactHandle> referenciasHechosActuales = new Vector<FactHandle>(); //vetor que guarda as coisas que ocorreram

    
    public Chimbinha(){
    }
    
    @Override
    public void run() {
    	DEBUG.habilitarModoDebug(System.getProperty("robot.debug", "true").equals("true"));    	
    	
    	setColors(Color.black,Color.yellow,Color.black); // body,gun,radar
    	
    	// Cria a base de conhecimento
    	crearBaseConocimiento();

        // Inicializa o movimento do radar, do canhão e do tanque em si
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);


        while (true) {
        	DEBUG.mensaje("inicio turno");
            cargarEstadoRobot();
            cargarEstadoBatalla();

            // Inicia todas as regras
            DEBUG.mensaje("hechos en memoria activa");
            DEBUG.volcarHechos(ksession);           
            ksession.fireAllRules();
            limpiarHechosIteracionAnterior();

            // Recupera as ações guardadas no vetor
            Vector<Accion> acciones = recuperarAcciones();
            DEBUG.mensaje("acciones resultantes");
            DEBUG.volcarAcciones(acciones);

            // Executa as ações
            ejecutarAcciones(acciones);
        	DEBUG.mensaje("fin turno\n");
            execute();

        }

    }


    private void crearBaseConocimiento() {
        String ficheroReglas = System.getProperty("robot.reglas", Chimbinha.FICHERO_REGLAS);

        DEBUG.mensaje("crear base de conocimientos");
        kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        DEBUG.mensaje("cargar reglas desde "+ficheroReglas);
        kbuilder.add(ResourceFactory.newClassPathResource(ficheroReglas, Chimbinha.class), ResourceType.DRL);
        if (kbuilder.hasErrors()) {
            System.err.println(kbuilder.getErrors().toString());
        }

        kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        
        DEBUG.mensaje("crear sesion (memoria activa)");
        ksession = kbase.newStatefulKnowledgeSession();
    }



    private void cargarEstadoRobot() {
    	EstadoRobot estadoRobot = new EstadoRobot(this);
        referenciasHechosActuales.add(ksession.insert(estadoRobot));
    }

    private void cargarEstadoBatalla() {
        EstadoBatalla estadoBatalla =
                new EstadoBatalla(getBattleFieldWidth(), getBattleFieldHeight(),
                getNumRounds(), getRoundNum(),
                getTime(),
                getOthers());
        referenciasHechosActuales.add(ksession.insert(estadoBatalla));
    }

    private void limpiarHechosIteracionAnterior() {
        for (FactHandle referenciaHecho : this.referenciasHechosActuales) {
            ksession.retract(referenciaHecho);
        }
        this.referenciasHechosActuales.clear();
    }

    private Vector<Accion> recuperarAcciones() {
        Accion accion;
        Vector<Accion> listaAcciones = new Vector<Accion>();

        for (QueryResultsRow resultado : ksession.getQueryResults(Chimbinha.CONSULTA_ACCIONES)) {
            accion = (Accion) resultado.get("accion");  
            accion.setRobot(this);                      
            listaAcciones.add(accion);
            ksession.retract(resultado.getFactHandle("accion")); 
        }

        return listaAcciones;
    }

    private void ejecutarAcciones(Vector<Accion> acciones) {
        for (Accion accion : acciones) {
            accion.iniciarEjecucion();
        }
    }

    // Insere no evento os eventos baseados nos fatos que ocorreram atualmente
    @Override
    public void onBulletHit(BulletHitEvent event) {
          referenciasHechosActuales.add(ksession.insert(event));
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        referenciasHechosActuales.add(ksession.insert(event));
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        referenciasHechosActuales.add(ksession.insert(event));
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        referenciasHechosActuales.add(ksession.insert(event));
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        referenciasHechosActuales.add(ksession.insert(event));
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        referenciasHechosActuales.add(ksession.insert(event));
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        referenciasHechosActuales.add(ksession.insert(event));
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        referenciasHechosActuales.add(ksession.insert(event));
    }


}
